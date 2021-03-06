/*
 * Copyright (c) 2019 Couchbase, Inc.
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

package com.couchbase.client.scala.kv.handlers

import com.couchbase.client.core.cnc.{RequestSpan, TracingIdentifiers}
import com.couchbase.client.core.msg.ResponseStatus
import com.couchbase.client.core.msg.kv._
import com.couchbase.client.core.retry.RetryStrategy
import com.couchbase.client.scala.HandlerParams
import com.couchbase.client.scala.kv.{DefaultErrors, MutationResult}
import com.couchbase.client.scala.util.Validate

import scala.compat.java8.OptionConverters._
import scala.concurrent.duration.Duration
import scala.util.{Success, Try}

/**
  * Handles requests and responses for KV touch operations.
  *
  * @since 1.0.0
  */
private[scala] class TouchHandler(hp: HandlerParams)
    extends KeyValueRequestHandler[TouchResponse, MutationResult] {

  def request[T](
      id: String,
      expiryEpochTimeSecs: Long,
      timeout: java.time.Duration,
      retryStrategy: RetryStrategy,
      parentSpan: Option[RequestSpan]
  ): Try[TouchRequest] = {
    val validations: Try[TouchRequest] = for {
      _ <- Validate.notNullOrEmpty(id, "id")
      _ <- Validate.notNull(expiryEpochTimeSecs, "expiry")
      _ <- Validate.notNull(timeout, "timeout")
      _ <- Validate.notNull(retryStrategy, "retryStrategy")
      _ <- Validate.notNull(parentSpan, "parentSpan")
    } yield null

    if (validations.isFailure) {
      validations
    } else {
      Success(
        new TouchRequest(
          timeout,
          hp.core.context(),
          hp.collectionIdentifier,
          retryStrategy,
          id,
          expiryEpochTimeSecs,
          hp.tracer.requestSpan(TracingIdentifiers.SPAN_REQUEST_KV_TOUCH, parentSpan.orNull)
        )
      )
    }
  }

  def response(
      request: KeyValueRequest[TouchResponse],
      id: String,
      response: TouchResponse
  ): MutationResult = {
    response.status() match {
      case ResponseStatus.SUCCESS =>
        MutationResult(response.cas(), response.mutationToken().asScala)
      case _ => throw DefaultErrors.throwOnBadResult(request, response)
    }
  }
}
