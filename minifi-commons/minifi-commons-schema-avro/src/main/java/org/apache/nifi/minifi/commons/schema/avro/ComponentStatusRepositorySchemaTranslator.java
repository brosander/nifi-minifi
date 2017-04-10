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

import org.apache.nifi.minifi.commons.schema.ComponentStatusRepositorySchema;
import org.apache.nifi.minifi.commons.schema.avro.generated.ComponentStatusRepository;

import java.util.Map;

public class ComponentStatusRepositorySchemaTranslator extends BaseAvroTranslator<ComponentStatusRepository, ComponentStatusRepository.Builder, ComponentStatusRepositorySchema> {
    public ComponentStatusRepositorySchemaTranslator() {
        super(ComponentStatusRepositorySchema::new, ComponentStatusRepository::newBuilder);
    }

    @Override
    protected void putToMap(Map<String, Object> map, ComponentStatusRepository avro) {
        putIfSet(map, ComponentStatusRepositorySchema.BUFFER_SIZE_KEY, avro.getBufferSize());
        putIfSet(map, ComponentStatusRepositorySchema.SNAPSHOT_FREQUENCY_KEY, avro.getBufferSize());
    }

    @Override
    protected void setOnBuilder(ComponentStatusRepository.Builder builder, ComponentStatusRepositorySchema schema) {
        builder.setBufferSize(schema.getBufferSize());
        builder.setSnapshotFrequency(schema.getSnapshotFrequency());
    }
}
