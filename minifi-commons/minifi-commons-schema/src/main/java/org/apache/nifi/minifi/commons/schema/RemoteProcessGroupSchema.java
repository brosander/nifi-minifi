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

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RemoteProcessGroupSchema extends AbstractRemoteProcessGroupSchema {
    public static final String S2S_PROXY_REQUIRES_HTTP = "Site-To-Site proxy support requires HTTP " + TRANSPORT_PROTOCOL_KEY;
    public static final String EXPECTED_PROXY_HOST_IF_PROXY_USER = "expected " + PROXY_HOST_KEY + " to be set if " + PROXY_USER_KEY + " is";
    public static final String EXPECTED_PROXY_USER_IF_PROXY_PASSWORD = "expected " + PROXY_USER_KEY + " to be set if " + PROXY_PASSWORD_KEY + " is";
    public static final String EXPECTED_PROXY_PASSWORD_IF_PROXY_USER = "expected " + PROXY_PASSWORD_KEY + " to be set if " + PROXY_USER_KEY + " is";

    public RemoteProcessGroupSchema(Map map) {
        super(map);
    }

    @Override
    public List<String> getValidationIssues() {
        List<String> validationIssues = super.getValidationIssues();
        if (getInputPorts().size() == 0 && getOutputPorts().size() == 0) {
            validationIssues.add("Expected either '" + INPUT_PORTS_KEY + "', '" + OUTPUT_PORTS_KEY + "' in section '" + getWrapperName() + "' to have value(s)");
        }
        if (StringUtil.isNullOrEmpty(getProxyHost())) {
            if (!StringUtil.isNullOrEmpty(getProxyUser())) {
                validationIssues.add(getIssueText(PROXY_USER_KEY, getWrapperName(), EXPECTED_PROXY_HOST_IF_PROXY_USER));
            }
        } else if (getTransportProtocol() != TransportProtocolOptions.HTTP) {
            validationIssues.add(getIssueText(PROXY_HOST_KEY, getWrapperName(), S2S_PROXY_REQUIRES_HTTP));
        }

        if (StringUtil.isNullOrEmpty(getProxyUser())) {
            if (!StringUtil.isNullOrEmpty(getProxyPassword())) {
                validationIssues.add(getIssueText(PROXY_PASSWORD_KEY, getWrapperName(), EXPECTED_PROXY_USER_IF_PROXY_PASSWORD));
            }
        } else if (StringUtil.isNullOrEmpty(getProxyPassword())) {
            validationIssues.add(getIssueText(PROXY_USER_KEY, getWrapperName(), EXPECTED_PROXY_PASSWORD_IF_PROXY_USER));
        }
        Collections.sort(validationIssues);
        return validationIssues;
    }
}
