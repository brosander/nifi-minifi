/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nifi.minifi.commons.schema.v1;

import org.apache.nifi.minifi.commons.schema.RemoteProcessGroupSchema;

import java.util.Map;

public class RemoteProcessGroupSchemaV1 extends AbstractRemoteProcessGroupSchemaV1 {
    public RemoteProcessGroupSchemaV1(Map map) {
        super(map);
    }

    @Override
    public RemoteProcessGroupSchema convert() {
        Map<String, Object> result = mapSupplier.get();
        result.put(NAME_KEY, getName());
        result.put(URL_KEY, getUrl());
        result.put(COMMENT_KEY, getComment());
        result.put(TIMEOUT_KEY, getTimeout());
        result.put(YIELD_PERIOD_KEY, getYieldPeriod());
        putListIfNotNull(result, INPUT_PORTS_KEY, getInputPorts());
        return new RemoteProcessGroupSchema(result);
    }

    @Override
    public int getVersion() {
        return ConfigSchemaV1.CONFIG_VERSION;
    }
}
