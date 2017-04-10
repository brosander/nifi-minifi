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

import org.apache.nifi.minifi.commons.schema.SensitivePropsSchema;
import org.apache.nifi.minifi.commons.schema.avro.generated.SensitiveProps;

import java.util.Map;

public class SensitivePropsSchemaTranslator extends BaseAvroTranslator<SensitiveProps, SensitiveProps.Builder, SensitivePropsSchema> {
    public SensitivePropsSchemaTranslator() {
        super(SensitivePropsSchema::new, SensitiveProps::newBuilder);
    }

    @Override
    protected void putToMap(Map<String, Object> map, SensitiveProps avro) {
        putIfSet(map, SensitivePropsSchema.KEY_KEY, avro.getKey());
        putIfSet(map, SensitivePropsSchema.ALGORITHM_KEY, avro.getAlgorithm());
        putIfSet(map, SensitivePropsSchema.PROVIDER_KEY, avro.getProvider());
    }

    @Override
    protected void setOnBuilder(SensitiveProps.Builder builder, SensitivePropsSchema schema) {
        builder.setKey(schema.getKey());
        builder.setAlgorithm(schema.getAlgorithm());
        builder.setProvider(schema.getProvider());
    }
}
