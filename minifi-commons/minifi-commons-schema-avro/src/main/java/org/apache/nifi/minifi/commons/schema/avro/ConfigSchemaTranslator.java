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

import org.apache.nifi.minifi.commons.schema.ConfigSchema;
import org.apache.nifi.minifi.commons.schema.avro.generated.Config;
import org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys;

import java.util.Map;

public class ConfigSchemaTranslator extends BaseAvroTranslator<Config, Config.Builder, ConfigSchema> {
    private final FlowControllerSchemaTranslator flowControllerSchemaTranslator = new FlowControllerSchemaTranslator();
    private final CorePropertiesSchemaTranslator corePropertiesSchemaTranslator = new CorePropertiesSchemaTranslator();
    private final FlowFileRepositorySchemaTranslator flowFileRepositorySchemaTranslator = new FlowFileRepositorySchemaTranslator();
    private final ContentRepositorySchemaTranslator contentRepositorySchemaTranslator = new ContentRepositorySchemaTranslator();
    private final ComponentStatusRepositorySchemaTranslator componentStatusRepositorySchemaTranslator = new ComponentStatusRepositorySchemaTranslator();
    private final SecurityPropertiesSchemaTranslator securityPropertiesSchemaTranslator = new SecurityPropertiesSchemaTranslator();

    public ConfigSchemaTranslator() {
        super(ConfigSchema::new, Config::newBuilder);
    }

    @Override
    protected void setOnBuilder(Config.Builder builder, ConfigSchema schema) {
        builder.setFlowControllerProperties(flowControllerSchemaTranslator.toAvro(schema.getFlowControllerProperties()));
        builder.setCoreProperties(corePropertiesSchemaTranslator.toAvro(schema.getCoreProperties()));
        builder.setFlowfileRepositoryProperties(flowFileRepositorySchemaTranslator.toAvro(schema.getFlowfileRepositoryProperties()));
        builder.setContentRepositoryProperties(contentRepositorySchemaTranslator.toAvro(schema.getContentRepositoryProperties()));
        builder.setComponentStatusRepositoryProperties(componentStatusRepositorySchemaTranslator.toAvro(schema.getComponentStatusRepositoryProperties()));
        builder.setSecurityProperties(securityPropertiesSchemaTranslator.toAvro(schema.getSecurityProperties()));
    }

    @Override
    protected void putToMap(Map<String, Object> map, Config avro) {
        putIfSet(map, CommonPropertyKeys.FLOW_CONTROLLER_PROPS_KEY, avro.getFlowControllerProperties(), flowControllerSchemaTranslator::toMap);
        putIfSet(map, CommonPropertyKeys.CORE_PROPS_KEY, avro.getCoreProperties(), corePropertiesSchemaTranslator::toMap);
        putIfSet(map, CommonPropertyKeys.FLOWFILE_REPO_KEY, avro.getFlowfileRepositoryProperties(), flowFileRepositorySchemaTranslator::toMap);
        putIfSet(map, CommonPropertyKeys.CONTENT_REPO_KEY, avro.getContentRepositoryProperties(), contentRepositorySchemaTranslator::toMap);
        putIfSet(map, CommonPropertyKeys.COMPONENT_STATUS_REPO_KEY, avro.getComponentStatusRepositoryProperties(), componentStatusRepositorySchemaTranslator::toMap);
        putIfSet(map, CommonPropertyKeys.SECURITY_PROPS_KEY, avro.getSecurityProperties(), securityPropertiesSchemaTranslator::toMap);
    }
}
