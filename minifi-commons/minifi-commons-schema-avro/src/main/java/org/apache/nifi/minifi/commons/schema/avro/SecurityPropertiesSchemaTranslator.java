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

import org.apache.nifi.minifi.commons.schema.SecurityPropertiesSchema;
import org.apache.nifi.minifi.commons.schema.avro.generated.SecurityProperties;
import org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys;

import java.util.Map;

public class SecurityPropertiesSchemaTranslator extends BaseAvroTranslator<SecurityProperties, SecurityProperties.Builder, SecurityPropertiesSchema> {
    private final SensitivePropsSchemaTranslator sensitivePropsSchemaTranslator = new SensitivePropsSchemaTranslator();

    public SecurityPropertiesSchemaTranslator() {
        super(SecurityPropertiesSchema::new, SecurityProperties::newBuilder);
    }

    @Override
    protected void putToMap(Map<String, Object> map, SecurityProperties avro) {
        putIfSet(map, SecurityPropertiesSchema.KEYSTORE_KEY, avro.getKeystore());
        putIfSet(map, SecurityPropertiesSchema.KEYSTORE_TYPE_KEY, avro.getKeystoreType());
        putIfSet(map, SecurityPropertiesSchema.KEYSTORE_PASSWORD_KEY, avro.getKeystorePassword());
        putIfSet(map, SecurityPropertiesSchema.KEY_PASSWORD_KEY, avro.getKeyPassword());
        putIfSet(map, SecurityPropertiesSchema.TRUSTSTORE_KEY, avro.getTruststore());
        putIfSet(map, SecurityPropertiesSchema.TRUSTSTORE_TYPE_KEY, avro.getTruststoreType());
        putIfSet(map, SecurityPropertiesSchema.TRUSTSTORE_PASSWORD_KEY, avro.getTruststorePassword());
        putIfSet(map, SecurityPropertiesSchema.SSL_PROTOCOL_KEY, avro.getSslProtocol());
        putIfSet(map, CommonPropertyKeys.SENSITIVE_PROPS_KEY, avro.getSensitiveProps(), sensitivePropsSchemaTranslator::toMap);
    }

    @Override
    protected void setOnBuilder(SecurityProperties.Builder builder, SecurityPropertiesSchema schema) {
        builder.setKeystore(schema.getKeystore());
        builder.setKeystoreType(schema.getKeystoreType());
        builder.setKeystorePassword(schema.getKeystorePassword());
        builder.setKeyPassword(schema.getKeyPassword());
        builder.setTruststore(schema.getTruststore());
        builder.setTruststoreType(schema.getTruststoreType());
        builder.setTruststorePassword(schema.getTruststorePassword());
        builder.setSslProtocol(schema.getSslProtocol());
        builder.setSensitiveProps(sensitivePropsSchemaTranslator.toAvro(schema.getSensitiveProps()));
    }
}
