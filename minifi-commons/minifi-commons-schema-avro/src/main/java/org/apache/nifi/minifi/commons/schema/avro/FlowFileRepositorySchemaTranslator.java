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

import org.apache.nifi.minifi.commons.schema.FlowFileRepositorySchema;
import org.apache.nifi.minifi.commons.schema.avro.generated.FlowFileRepository;
import org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys;

import java.util.Map;

public class FlowFileRepositorySchemaTranslator extends BaseAvroTranslator<FlowFileRepository, FlowFileRepository.Builder, FlowFileRepositorySchema> {
    private final SwapSchemaTranslator swapSchemaTranslator;

    public FlowFileRepositorySchemaTranslator() {
        super(FlowFileRepositorySchema::new, FlowFileRepository::newBuilder);
        swapSchemaTranslator = new SwapSchemaTranslator();
    }

    @Override
    protected void putToMap(Map<String, Object> map, FlowFileRepository avro) {
        putIfSet(map, FlowFileRepositorySchema.PARTITIONS_KEY, avro.getPartitions());
        putIfSet(map, FlowFileRepositorySchema.CHECKPOINT_INTERVAL_KEY, avro.getCheckpointInterval());
        putIfSet(map, CommonPropertyKeys.ALWAYS_SYNC_KEY, avro.getAlwaysSync());
        putIfSet(map, CommonPropertyKeys.SWAP_PROPS_KEY, avro.getSwapProperties(), swapSchemaTranslator::toMap);
    }

    @Override
    protected void setOnBuilder(FlowFileRepository.Builder builder, FlowFileRepositorySchema schema) {
        builder.setPartitions(schema.getPartitions());
        builder.setCheckpointInterval(schema.getCheckpointInterval());
        builder.setAlwaysSync(schema.getAlwaysSync());
        builder.setSwapProperties(swapSchemaTranslator.toAvro(schema.getSwapProperties()));
    }
}
