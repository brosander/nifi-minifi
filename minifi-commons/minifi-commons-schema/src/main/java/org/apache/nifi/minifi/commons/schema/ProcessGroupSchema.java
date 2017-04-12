/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.apache.nifi.minifi.commons.schema;

import org.apache.nifi.minifi.commons.schema.common.ConvertableSchema;
import org.apache.nifi.minifi.commons.schema.common.StringUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.INPUT_PORTS_KEY;
import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.OUTPUT_PORTS_KEY;

public class ProcessGroupSchema extends AbstractProcessGroupSchema implements ConvertableSchema<ProcessGroupSchema> {
    public static final String PROCESS_GROUPS_KEY = "Process Groups";

    public ProcessGroupSchema() {
        this(Collections.emptyMap(), ConfigSchema.WRAPPER_NAME);
    }

    public ProcessGroupSchema(Map map, String wrapperName) {
        super(map, wrapperName);

        if (ConfigSchema.WRAPPER_NAME.equals(wrapperName)) {
            if (getInputPortSchemas().size() > 0) {
                addValidationIssue(INPUT_PORTS_KEY, wrapperName, "must be empty in root group as external input/output ports are currently unsupported");
            }
            if (getOutputPortSchemas().size() > 0) {
                addValidationIssue(OUTPUT_PORTS_KEY, wrapperName, "must be empty in root group as external input/output ports are currently unsupported");
            }
        } else if (ID_DEFAULT.equals(getId())) {
            addValidationIssue(ID_KEY, wrapperName, "must be set to a value not " + ID_DEFAULT + " if not in root group");
        }
    }

    @Override
    public void initValidation() {
        Set<String> portIds = getPortIds();
        getConnections().stream().filter(c -> portIds.contains(c.getSourceId())).forEachOrdered(c -> c.setNeedsSourceRelationships(false));

        Set<String> funnelIds = new HashSet<>(getFunnels().stream().map(FunnelSchema::getId).collect(Collectors.toList()));
        getConnections().stream().filter(c -> funnelIds.contains(c.getSourceId())).forEachOrdered(c -> c.setNeedsSourceRelationships(false));

        super.initValidation();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = super.toMap();
        if (ID_DEFAULT.equals(getId())) {
            result.remove(ID_KEY);
        }
        if (StringUtil.isNullOrEmpty(getName())) {
            result.remove(NAME_KEY);
        }
        return result;
    }

    public Set<String> getPortIds() {
        Set<String> result = new HashSet<>();
        getInputPortSchemas().stream().map(PortSchema::getId).forEachOrdered(result::add);
        getOutputPortSchemas().stream().map(PortSchema::getId).forEachOrdered(result::add);
        getRemoteProcessGroups().stream().flatMap(r -> r.getInputPorts().stream()).map(RemotePortSchema::getId).forEachOrdered(result::add);
        getRemoteProcessGroups().stream().flatMap(r -> r.getOutputPorts().stream()).map(RemotePortSchema::getId).forEachOrdered(result::add);
        getProcessGroupSchemas().stream().flatMap(p -> p.getPortIds().stream()).forEachOrdered(result::add);
        return result;
    }

    @Override
    protected boolean isValidId(String value) {
        if (ID_DEFAULT.equals(value)) {
            return true;
        }
        return super.isValidId(value);
    }

    @Override
    public ProcessGroupSchema convert() {
        return this;
    }

    @Override
    public int getVersion() {
        return ConfigSchema.CONFIG_VERSION;
    }
}
