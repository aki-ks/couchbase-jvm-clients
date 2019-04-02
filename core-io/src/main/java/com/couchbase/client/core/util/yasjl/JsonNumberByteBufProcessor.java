/*
 * Copyright (c) 2017 Couchbase, Inc.
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

package com.couchbase.client.core.util.yasjl;

import com.couchbase.client.core.deps.io.netty.util.ByteProcessor;

import static com.couchbase.client.core.util.yasjl.JsonParserUtils.JSON_MINUS;
import static com.couchbase.client.core.util.yasjl.JsonParserUtils.JSON_PLUS;

/**
 * Process JSON number
 *
 * @author Subhashni Balakrishnan
 */
public class JsonNumberByteBufProcessor implements ByteProcessor {

    //not verifying if valid
    public boolean process(byte value) throws Exception {
        if (value == (byte) 'e' || value == (byte) 'E') {
            return true;
        }
        if (value >= (byte) '0' && value <= (byte) '9') {
            return true;
        }
        return value == JSON_MINUS || value == JSON_PLUS || value == (byte) '.';
    }
}
