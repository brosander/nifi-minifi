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
package org.apache.nifi.minifi.bootstrap.util.schema;

import org.apache.nifi.minifi.bootstrap.util.schema.common.BaseSchema;
import org.apache.nifi.web.api.dto.RemoteProcessGroupDTO;

import java.util.List;
import java.util.Map;

import static org.apache.nifi.minifi.bootstrap.util.schema.common.CommonPropertyKeys.COMMENT_KEY;
import static org.apache.nifi.minifi.bootstrap.util.schema.common.CommonPropertyKeys.INPUT_PORTS_KEY;
import static org.apache.nifi.minifi.bootstrap.util.schema.common.CommonPropertyKeys.NAME_KEY;
import static org.apache.nifi.minifi.bootstrap.util.schema.common.CommonPropertyKeys.REMOTE_PROCESSING_GROUPS_KEY;
import static org.apache.nifi.minifi.bootstrap.util.schema.common.CommonPropertyKeys.YIELD_PERIOD_KEY;

/**
 *
 */
public class RemoteProcessingGroupSchema extends BaseSchema {
    public static final String URL_KEY = "url";
    public static final String TIMEOUT_KEY = "timeout";

    private String name;
    private String comment = "";
    private String url;
    private String timeout = "30 secs";
    private String yieldPeriod = "10 sec";
    private List<RemoteInputPortSchema> inputPorts;

    public RemoteProcessingGroupSchema() {
    }

    public RemoteProcessingGroupSchema(RemoteProcessGroupDTO remoteProcessGroupDTO) {
        this.name = remoteProcessGroupDTO.getName();
        this.comment = remoteProcessGroupDTO.getComments();
        this.url = remoteProcessGroupDTO.getTargetUri();
        this.timeout = remoteProcessGroupDTO.getCommunicationsTimeout();
        this.inputPorts = null;//TODO
    }

    public RemoteProcessingGroupSchema(Map map) {
        name = getRequiredKeyAsType(map, NAME_KEY, String.class, REMOTE_PROCESSING_GROUPS_KEY);
        url = getRequiredKeyAsType(map, URL_KEY, String.class, REMOTE_PROCESSING_GROUPS_KEY);

        comment = getOptionalKeyAsType(map, COMMENT_KEY, String.class, REMOTE_PROCESSING_GROUPS_KEY, "");
        timeout = getOptionalKeyAsType(map, TIMEOUT_KEY, String.class, REMOTE_PROCESSING_GROUPS_KEY, "30 secs");
        yieldPeriod = getOptionalKeyAsType(map, YIELD_PERIOD_KEY, String.class, REMOTE_PROCESSING_GROUPS_KEY, "10 sec");

        inputPorts = getRequiredKeyAsType(map, INPUT_PORTS_KEY, List.class, REMOTE_PROCESSING_GROUPS_KEY);
        if (inputPorts != null) {
            transformListToType(inputPorts, "input port", RemoteInputPortSchema.class, INPUT_PORTS_KEY);

            for (RemoteInputPortSchema remoteInputPortSchema: inputPorts) {
                addIssuesIfNotNull(remoteInputPortSchema);
            }
        }
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> result = super.toMap();
        result.put(NAME_KEY, name);
        result.put(URL_KEY, url);
        result.put(COMMENT_KEY, comment);
        result.put(TIMEOUT_KEY, timeout);
        result.put(YIELD_PERIOD_KEY, yieldPeriod);
        putListIfNotNull(result, INPUT_PORTS_KEY, inputPorts);
        return result;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public String getUrl() {
        return url;
    }

    public String getTimeout() {
        return timeout;
    }

    public String getYieldPeriod() {
        return yieldPeriod;
    }

    public List<RemoteInputPortSchema> getInputPorts() {
        return inputPorts;
    }
}
