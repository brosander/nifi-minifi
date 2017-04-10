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

import org.apache.nifi.minifi.commons.schema.ControllerServiceSchema;
import org.apache.nifi.minifi.commons.schema.avro.generated.ControllerService;
import org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys;

import java.util.Map;

public class ControllerServiceSchemaTranslator extends BaseAvroTranslator<ControllerService, ControllerService.Builder, ControllerServiceSchema> {
    public ControllerServiceSchemaTranslator() {
        super(ControllerServiceSchema::new, ControllerService::newBuilder);
    }

    @Override
    protected void putToMap(Map<String, Object> map, ControllerService avro) {
        putIfSet(map, CommonPropertyKeys.ID_KEY, avro.getId());
        putIfSet(map, CommonPropertyKeys.NAME_KEY, avro.getName());
        putIfSet(map, CommonPropertyKeys.PROPERTIES_KEY, avro.getProperties(), this::toStringMap);
        putIfSet(map, CommonPropertyKeys.ANNOTATION_DATA_KEY, avro.getAnnotationData());
        putIfSet(map, CommonPropertyKeys.TYPE_KEY, avro.getServiceClass());
    }

    @Override
    protected void setOnBuilder(ControllerService.Builder builder, ControllerServiceSchema schema) {
        builder.setProperties(toCharSequenceMap(schema.getProperties()));
        builder.setAnnotationData(schema.getAnnotationData());
        builder.setServiceClass(schema.getServiceClass());
    }
}
