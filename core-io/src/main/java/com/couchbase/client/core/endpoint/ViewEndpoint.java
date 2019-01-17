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

package com.couchbase.client.core.endpoint;

import com.couchbase.client.core.CoreContext;
import com.couchbase.client.core.io.NetworkAddress;
import com.couchbase.client.core.io.netty.query.QueryMessageHandler;
import com.couchbase.client.core.service.ServiceType;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ViewEndpoint extends BaseEndpoint {

  private final CoreContext coreContext;

  public ViewEndpoint(final CoreContext coreContext, final NetworkAddress hostname,
                      final int port) {
    super(hostname, port, coreContext.environment().ioEnvironment().viewEventLoopGroup().get(),
      coreContext, coreContext.environment().ioEnvironment().viewCircuitBreakerConfig(), ServiceType.VIEWS);
    this.coreContext = coreContext;
  }

  @Override
  protected PipelineInitializer pipelineInitializer() {
    return new ViewPipelineInitializer(coreContext);
  }

  public static class ViewPipelineInitializer implements PipelineInitializer {

    private final CoreContext coreContext;

    public ViewPipelineInitializer(CoreContext coreContext) {
      this.coreContext = coreContext;
    }

    @Override
    public void init(ChannelPipeline pipeline) {
      pipeline.addLast(new LoggingHandler(LogLevel.ERROR));
      pipeline.addLast(new HttpClientCodec());
      //pipeline.addLast(new QueryMessageHandler());
    }
  }

}
