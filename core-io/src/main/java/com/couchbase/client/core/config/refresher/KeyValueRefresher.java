/*
 * Copyright (c) 2016 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.couchbase.client.core.config.refresher;

import com.couchbase.client.core.Core;
import com.couchbase.client.core.CoreContext;
import com.couchbase.client.core.Reactor;
import com.couchbase.client.core.annotation.Stability;
import com.couchbase.client.core.config.BucketConfig;
import com.couchbase.client.core.config.ClusterConfig;
import com.couchbase.client.core.config.ConfigurationProvider;
import com.couchbase.client.core.config.NodeInfo;
import com.couchbase.client.core.config.ProposedBucketConfigContext;
import com.couchbase.client.core.msg.kv.CarrierBucketConfigRequest;
import com.couchbase.client.core.retry.BestEffortRetryStrategy;
import com.couchbase.client.core.service.ServiceType;
import io.netty.util.CharsetUtil;
import reactor.core.Disposable;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The {@link KeyValueRefresher} keeps configs up-to-date through the KV service.
 *
 * @since 1.0.0
 */
@Stability.Internal
public class KeyValueRefresher implements Refresher {

  /**
   * The interval at which the poller fires.
   *
   * <p>Note that this does not mean that a new config is request at this interval, since
   * those are checked individually based on the configuration applied (but it specifies the
   * minimum interval possible).</p>
   */
  private static final Duration POLLER_INTERVAL = Duration.ofSeconds(1);

  /**
   * The number of config fetches in parallel at maximum per interval.
   */
  private static final int MAX_PARALLEL_FETCH = 3;

  /**
   * Holds the core as a reference.
   */
  private final Core core;

  /**
   * The registration which is used to track the interval polls and needs to be disposed
   * on shutdown.
   */
  private final Disposable pollRegistration;

  /**
   * Holds the config provider as a reference.
   */
  private final ConfigurationProvider provider;

  /**
   * The global offset is used to make sure each KV node eventually gets used
   * by shifting on each invocation.
   */
  private final AtomicLong nodeOffset = new AtomicLong(0);

  /**
   * Holds all current registrations and their last timestamp when the bucket has been
   * updated.
   */
  private final Map<String, Long> registrations = new ConcurrentHashMap<>();

  /**
   * Holds all tainted bucket configs (those undergoing rebalance at the moment).
   */
  private final Set<String> tainted = Collections.synchronizedSet(new HashSet<>());

  /**
   * Holds the config stream to be subscribed to for the config events on this refresher.
   */
  private final DirectProcessor<ProposedBucketConfigContext> configs = DirectProcessor.create();

  /**
   * This sink should be used to push configs out to subscribers to be thread-safe.
   */
  private final FluxSink<ProposedBucketConfigContext> configsSink = configs.sink();

  private final long configPollInterval;


  public KeyValueRefresher(final ConfigurationProvider provider, final Core core) {
    this.core = core;
    this.provider = provider;
    this.configPollInterval = core.context().environment().configPollInterval().toNanos();

    pollRegistration = Flux
      .interval(POLLER_INTERVAL)
      .filter(v -> !registrations.isEmpty())
      .flatMap(ignored -> Flux
        .fromIterable(registrations.keySet())
        .flatMap(KeyValueRefresher.this::maybeUpdateBucket)
      )
      .subscribe(configsSink::next);
  }

  @Override
  public Flux<ProposedBucketConfigContext> configs() {
    return configs;
  }

  /**
   * If needed, fetches a configuration (and otherwise ignores this attempt).
   *
   * <p>This method makes sure that individual errors are logged but swallowed so they do
   * not interrupt anything else in progress.</p>
   *
   * @param name the name of the bucket.
   * @return a {@link Mono} either with a new config or nothing to ignore.
   */
  private Mono<ProposedBucketConfigContext> maybeUpdateBucket(final String name) {
    Long last = registrations.get(name);
    boolean overInterval = last != null && (System.nanoTime() - last) >= configPollInterval;
    boolean allowed = tainted.contains(name) || overInterval;

    return allowed
      ? fetchConfigPerNode(name, filterEligibleNodes(name))
        .next()
        .doOnSuccess(ctx -> registrations.replace(name, System.nanoTime()))
      : Mono.empty();
  }

  /**
   * Helper method to generate a stream of KV nodes that can be used to fetch a config.
   *
   * <p>To be more resilient to failures, this method (similar to 1.x) shifts the node
   * list on each invocation by one to make sure we hit all of them eventually.</p>
   *
   * @param name the bucket name.
   * @return a flux of nodes that have the KV service enabled.
   */
  private Flux<NodeInfo> filterEligibleNodes(final String name) {
    return Flux.defer(() -> {
      BucketConfig config = provider.config().bucketConfig(name);
      if (config == null) {
        // todo: log debug that no node found to refresh a config from
        return Flux.empty();
      }

      List<NodeInfo> nodes = new ArrayList<>(config.nodes());
      shiftNodeList(nodes);

      return Flux
        .fromIterable(nodes)
        .filter(n -> n.services().containsKey(ServiceType.KV)
          || n.sslServices().containsKey(ServiceType.KV))
        .take(MAX_PARALLEL_FETCH);
    });
  }

  /**
   * Helper method to fetch a config per node provided.
   *
   * @param name the bucket name.
   * @param nodes the flux of nodes that can be used to fetch a config.
   * @return returns configs for each node if found.
   */
  private Flux<ProposedBucketConfigContext> fetchConfigPerNode(final String name,
                                                               final Flux<NodeInfo> nodes) {
    return nodes.flatMap(nodeInfo -> {
      CoreContext ctx = core.context();
      CarrierBucketConfigRequest request = new CarrierBucketConfigRequest(
        ctx.environment().kvTimeout(),
        ctx,
        name,
        BestEffortRetryStrategy.INSTANCE,
        nodeInfo.hostname()
      );
      core.send(request);
      return Reactor
        .wrap(request, request.response(), true)
        .filter(response -> {
          // TODO: debug event that it got ignored.
          return response.status().success();
        })
        .map(response -> {
          String raw = new String(response.content(), CharsetUtil.UTF_8);
          raw = raw.replace("$HOST", nodeInfo.hostname().address());
          return new ProposedBucketConfigContext(name, raw, nodeInfo.hostname());
        }).onErrorResume(t -> {
          // TODO: raise a warning that fetching a config individual failed.
          return Mono.empty();
        });
    });
  }

  /**
   * Helper method to transparently rearrange the node list based on the current global offset.
   *
   * @param nodeList the list to shift.
   */
  private <T> void shiftNodeList(final List<T> nodeList) {
    int shiftBy = (int) (nodeOffset.getAndIncrement() % nodeList.size());
    for(int i = 0; i < shiftBy; i++) {
      T element = nodeList.remove(0);
      nodeList.add(element);
    }
  }

  @Override
  public Mono<Void> register(final String name) {
    return Mono.defer(() -> {
      registrations.put(name, 0L);
      return Mono.empty();
    });
  }

  @Override
  public Mono<Void> deregister(final String name) {
    return Mono.defer(() -> {
      registrations.remove(name);
      return Mono.empty();
    });
  }

  @Override
  public void markTainted(final String name) {
    tainted.add(name);
  }

  @Override
  public void markUntainted(final String name) {
    tainted.remove(name);
  }

  @Override
  public Mono<Void> shutdown() {
    return Mono.defer(() -> {
      if (!pollRegistration.isDisposed()) {
        pollRegistration.dispose();
      }
      configsSink.complete();
      return Mono.empty();
    });
  }

}