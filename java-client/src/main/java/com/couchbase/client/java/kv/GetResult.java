/*
 * Copyright (c) 2018 Couchbase, Inc.
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

package com.couchbase.client.java.kv;

import com.couchbase.client.java.codec.Decoder;
import com.couchbase.client.java.codec.DefaultDecoder;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;

import java.time.Duration;
import java.util.Optional;

/**
 * Experimental prototype for a different result type on fetch.
 */
public class GetResult {

  private final String id;
  private final EncodedDocument encoded;
  private final long cas;
  private final Optional<Duration> expiration;

  static GetResult create(final String id, final EncodedDocument encoded,
                          final long cas, final Optional<Duration> expiration)
  {
    return new GetResult(id, encoded, cas, expiration);
  }

  private GetResult(final String id, final EncodedDocument encoded, final long cas,
                    final Optional<Duration> expiration) {
    this.id = id;
    this.cas = cas;
    this.encoded = encoded;
    this.expiration = expiration;
  }

  public String id() {
    return id;
  }

  public long cas() {
    return cas;
  }

  public Optional<Duration> expiration() {
    return expiration;
  }

  public JsonObject contentAsObject() {
    return contentAs(JsonObject.class);
  }

  public JsonArray contentAsArray() {
    return contentAs(JsonArray.class);
  }

  @SuppressWarnings({ "unchecked" })
  public <T> T contentAs(final Class<T> target) {
    return contentAs(target, (Decoder<T>) DefaultDecoder.INSTANCE);
  }

  public <T> T contentAs(final Class<T> target, final Decoder<T> decoder) {
    return decoder.decode(target, encoded);
  }

  @Override
  public String toString() {
    return "GetResult{" +
      "id='" + id + '\'' +
      ", encoded=" + encoded +
      ", cas=" + cas +
      ", expiration=" + expiration +
      '}';
  }

}
