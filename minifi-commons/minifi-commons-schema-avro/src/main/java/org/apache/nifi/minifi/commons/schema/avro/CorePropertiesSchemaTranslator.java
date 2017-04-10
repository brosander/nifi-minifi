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

package org.apache.nifi.minifi.commons.schema.avro;

import org.apache.nifi.minifi.commons.schema.CorePropertiesSchema;
import org.apache.nifi.minifi.commons.schema.avro.generated.CoreProperties;
import org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys;

import java.util.Map;

public class CorePropertiesSchemaTranslator extends BaseAvroTranslator<CoreProperties, CoreProperties.Builder, CorePropertiesSchema> {
    public CorePropertiesSchemaTranslator() {
        super(CorePropertiesSchema::new, CoreProperties::newBuilder);
    }

    @Override
    protected void putToMap(Map<String, Object> map, CoreProperties avro) {
        putIfSet(map, CorePropertiesSchema.FLOW_CONTROLLER_GRACEFUL_SHUTDOWN_PERIOD_KEY, avro.getFlowControllerGracefulShutdownPeriod());
        putIfSet(map, CorePropertiesSchema.FLOW_SERVICE_WRITE_DELAY_INTERVAL_KEY, avro.getFlowServiceWriteDelayInterval());
        putIfSet(map, CorePropertiesSchema.ADMINISTRATIVE_YIELD_DURATION_KEY, avro.getAdministrativeYieldDuration());
        putIfSet(map, CorePropertiesSchema.BORED_YIELD_DURATION_KEY, avro.getBoredYieldDuration());
        putIfSet(map, CommonPropertyKeys.MAX_CONCURRENT_THREADS_KEY, avro.getMaxConcurrentThreads());
    }

    @Override
    protected void setOnBuilder(CoreProperties.Builder builder, CorePropertiesSchema schema) {
        builder.setFlowControllerGracefulShutdownPeriod(schema.getFlowControllerGracefulShutdownPeriod());
        builder.setFlowServiceWriteDelayInterval(schema.getFlowServiceWriteDelayInterval());
        builder.setAdministrativeYieldDuration(schema.getAdministrativeYieldDuration());
        builder.setBoredYieldDuration(schema.getBoredYieldDuration());
        builder.setMaxConcurrentThreads(schema.getMaxConcurrentThreads());
    }
}
