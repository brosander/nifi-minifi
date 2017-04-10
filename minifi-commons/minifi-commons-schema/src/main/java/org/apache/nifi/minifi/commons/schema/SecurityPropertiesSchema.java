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

package org.apache.nifi.minifi.commons.schema;

import org.apache.nifi.minifi.commons.schema.common.StringUtil;

import java.util.Map;

import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.SECURITY_PROPS_KEY;

public class SecurityPropertiesSchema extends AbstractSecurityPropertiesSchema {
    public SecurityPropertiesSchema() {
        super();
    }

    public SecurityPropertiesSchema(Map map) {
        super(map);

        if (!StringUtil.isNullOrEmpty(getKeystoreType())) {
            if (validateStoreType(getKeystoreType())) {
                addValidationIssue(KEYSTORE_TYPE_KEY, SECURITY_PROPS_KEY, "it is not a supported type (must be either PKCS12 or JKS format)");
            }
        }

        if (!StringUtil.isNullOrEmpty(getTruststoreType())) {
            if (validateStoreType(getTruststoreType())) {
                addValidationIssue(TRUSTSTORE_TYPE_KEY, SECURITY_PROPS_KEY, "it is not a supported type (must be either PKCS12 or JKS format)");
            }
        }

        if (!StringUtil.isNullOrEmpty(getSslProtocol())) {
            switch (getSslProtocol()) {
                case "SSL":
                    break;
                case "SSLv2Hello":
                    break;
                case "SSLv3":
                    break;
                case "TLS":
                    break;
                case "TLSv1":
                    break;
                case "TLSv1.1":
                    break;
                case "TLSv1.2":
                    break;
                default:
                    addValidationIssue(SSL_PROTOCOL_KEY, SECURITY_PROPS_KEY, "it is not an allowable value of SSL protocol");
                    break;
            }
            if (StringUtil.isNullOrEmpty(getKeystore())) {
                addValidationIssue("When the '" + SSL_PROTOCOL_KEY + "' key of '" + SECURITY_PROPS_KEY + "' is set, the '" + KEYSTORE_KEY + "' must also be set");
            } else if (StringUtil.isNullOrEmpty(getKeystoreType()) || StringUtil.isNullOrEmpty(getKeystorePassword()) || StringUtil.isNullOrEmpty(getKeyPassword())) {
                addValidationIssue("When the '" + KEYSTORE_KEY + "' key of '" + SECURITY_PROPS_KEY + "' is set, the '" + KEYSTORE_TYPE_KEY + "', '" + KEYSTORE_PASSWORD_KEY +
                        "' and '" + KEY_PASSWORD_KEY + "' all must also be set");
            }

            if (!StringUtil.isNullOrEmpty(getTruststore()) && (StringUtil.isNullOrEmpty(getTruststoreType()) || StringUtil.isNullOrEmpty(getTruststorePassword()))) {
                addValidationIssue("When the '" + TRUSTSTORE_KEY + "' key of '" + SECURITY_PROPS_KEY + "' is set, the '" + TRUSTSTORE_TYPE_KEY + "' and '" +
                        TRUSTSTORE_PASSWORD_KEY + "' must also be set");
            }
        }
    }

    private boolean validateStoreType(String store) {
        return !store.isEmpty() && !(store.equalsIgnoreCase("JKS") || store.equalsIgnoreCase("PKCS12"));
    }

    public boolean useSSL() {
        return !StringUtil.isNullOrEmpty(getSslProtocol());
    }
}
