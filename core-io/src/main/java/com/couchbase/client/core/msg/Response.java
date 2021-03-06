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

package com.couchbase.client.core.msg;

/**
 * This interface is the base entity for all Responses flowing through the client.
 *
 * @since 2.0.0
 */
public interface Response {

  /**
   * Holds the status of the response.
   *
   * <p>Note that it might indicate a successful response or an error of some other sorts. Please
   * see the enum for further description of the potential states it can be in.</p>
   *
   * @return the status for this response.
   */
  ResponseStatus status();

}
