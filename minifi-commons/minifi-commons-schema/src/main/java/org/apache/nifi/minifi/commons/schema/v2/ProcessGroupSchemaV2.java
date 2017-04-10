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

package org.apache.nifi.minifi.commons.schema.v2;

import org.apache.nifi.minifi.commons.schema.ConfigSchema;
import org.apache.nifi.minifi.commons.schema.FunnelSchema;
import org.apache.nifi.minifi.commons.schema.PortSchema;
import org.apache.nifi.minifi.commons.schema.ProcessGroupSchema;
import org.apache.nifi.minifi.commons.schema.common.StringUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.CONTROLLER_SERVICES_KEY;
import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.DEFAULT_PROPERTIES;
import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.INPUT_PORTS_KEY;
import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.OUTPUT_PORTS_KEY;

public class ProcessGroupSchemaV2 extends AbstractProcessGroupSchemaV2 {
    public ProcessGroupSchemaV2() {
        this(Collections.emptyMap(), ConfigSchema.TOP_LEVEL);
    }


    public ProcessGroupSchemaV2(Map map, String wrapperName) {
        super(map, wrapperName);
    }

    @Override
    public void initValidation() {
        if (ConfigSchema.TOP_LEVEL.equals(getWrapperName())) {
            if (getInputPortSchemas().size() > 0) {
                addValidationIssue(INPUT_PORTS_KEY, getWrapperName(), "must be empty in root group as external input/output ports are currently unsupported");
            }
            if (getOutputPortSchemas().size() > 0) {
                addValidationIssue(OUTPUT_PORTS_KEY, getWrapperName(), "must be empty in root group as external input/output ports are currently unsupported");
            }
        } else if (ID_DEFAULT.equals(getId())) {
            addValidationIssue(ID_KEY, getWrapperName(), "must be set to a value not " + ID_DEFAULT + " if not in root group");
        }

        Set<String> portIds = getPortIds();
        getConnections().stream().filter(c -> portIds.contains(c.getSourceId())).forEachOrdered(c -> c.setNeedsSourceRelationships(false));


        Set<String> funnelIds = new HashSet<>(getFunnels().stream().map(FunnelSchema::getId).collect(Collectors.toList()));
        getConnections().stream().filter(c -> funnelIds.contains(c.getSourceId())).forEachOrdered(c -> c.setNeedsSourceRelationships(false));

        super.initValidation();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = mapSupplier.get();
        String id = getId();
        if (!ID_DEFAULT.equals(id)) {
            result.put(ID_KEY, id);
        }
        StringUtil.doIfNotNullOrEmpty(getName(), name -> result.put(NAME_KEY, name));
        putListIfNotNull(result, PROCESSORS_KEY, getProcessors());
        putListIfNotNull(result, PROCESS_GROUP_SCHEMAS_KEY, getProcessGroupSchemas());
        putListIfNotNull(result, INPUT_PORTS_KEY, getInputPortSchemas());
        putListIfNotNull(result, OUTPUT_PORTS_KEY, getOutputPortSchemas());
        putListIfNotNull(result, FUNNELS_KEY, getFunnels());
        putListIfNotNull(result, CONNECTIONS_KEY, getConnections());
        putListIfNotNull(result, REMOTE_PROCESS_GROUPS_KEY, getRemoteProcessGroups());
        return result;
    }

    public Set<String> getPortIds() {
        Set<String> result = new HashSet<>();
        getInputPortSchemas().stream().map(PortSchema::getId).forEachOrdered(result::add);
        getOutputPortSchemas().stream().map(PortSchema::getId).forEachOrdered(result::add);
        getProcessGroupSchemas().stream().flatMap(p -> p.getPortIds().stream()).forEachOrdered(result::add);
        return result;
    }

    @Override
    public ProcessGroupSchema convert() {
        Map<String, Object> map = this.toMap();
        map.put(CONTROLLER_SERVICES_KEY, DEFAULT_PROPERTIES);
        return new ProcessGroupSchema(map, getWrapperName());
    }

    @Override
    public int getVersion() {
        return ConfigSchemaV2.CONFIG_VERSION;
    }

    @Override
    protected boolean isValidId(String value) {
        if (ID_DEFAULT.equals(value)) {
            return true;
        }
        return super.isValidId(value);
    }

    @Override
    protected List<ProcessGroupSchemaV2> initializeProcessGroupSchemas(Map map) {
        return getOptionalKeyAsList(map, PROCESS_GROUP_SCHEMAS_KEY, m -> new ProcessGroupSchemaV2(m, "ProcessGroup(id: {id}, name: {name})"), getWrapperName());
    }

    @Override
    protected List<PortSchema> initializeInputPortSchemas(Map map) {
        return getOptionalKeyAsList(map, INPUT_PORT_SCHEMAS_KEY, m -> new PortSchema(m, "InputPort(id: {id}, name: {name})"), getWrapperName());
    }

    @Override
    protected List<PortSchema> initializeOutputPortSchemas(Map map) {
        return getOptionalKeyAsList(map, OUTPUT_PORT_SCHEMAS_KEY, m -> new PortSchema(m, "OutputPort(id: {id}, name: {name})"), getWrapperName());
    }
}
