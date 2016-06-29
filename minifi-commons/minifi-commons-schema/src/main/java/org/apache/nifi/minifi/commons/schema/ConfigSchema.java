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

import org.apache.nifi.controller.Template;
import org.apache.nifi.web.api.dto.FlowSnippetDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.nifi.minifi.commons.schema.CommonPropertyKeys.COMPONENT_STATUS_REPO_KEY;
import static org.apache.nifi.minifi.commons.schema.CommonPropertyKeys.CONNECTIONS_KEY;
import static org.apache.nifi.minifi.commons.schema.CommonPropertyKeys.CONTENT_REPO_KEY;
import static org.apache.nifi.minifi.commons.schema.CommonPropertyKeys.CORE_PROPS_KEY;
import static org.apache.nifi.minifi.commons.schema.CommonPropertyKeys.FLOWFILE_REPO_KEY;
import static org.apache.nifi.minifi.commons.schema.CommonPropertyKeys.FLOW_CONTROLLER_PROPS_KEY;
import static org.apache.nifi.minifi.commons.schema.CommonPropertyKeys.PROCESSORS_KEY;
import static org.apache.nifi.minifi.commons.schema.CommonPropertyKeys.PROVENANCE_REPORTING_KEY;
import static org.apache.nifi.minifi.commons.schema.CommonPropertyKeys.PROVENANCE_REPO_KEY;
import static org.apache.nifi.minifi.commons.schema.CommonPropertyKeys.REMOTE_PROCESSING_GROUPS_KEY;
import static org.apache.nifi.minifi.commons.schema.CommonPropertyKeys.SECURITY_PROPS_KEY;

/**
 *
 */
public class ConfigSchema extends BaseSchema {
    public static String TOP_LEVEL_NAME = "top level";

    private FlowControllerSchema flowControllerProperties;
    private CorePropertiesSchema coreProperties;
    private FlowFileRepositorySchema flowfileRepositoryProperties;
    private ContentRepositorySchema contentRepositoryProperties;
    private ComponentStatusRepositorySchema componentStatusRepositoryProperties;
    private SecurityPropertiesSchema securityProperties;
    private List<ProcessorSchema> processors;
    private List<ConnectionSchema> connections;
    private List<RemoteProcessingGroupSchema> remoteProcessingGroups;
    private ProvenanceReportingSchema provenanceReportingProperties;

    private ProvenanceRepositorySchema provenanceRepositorySchema;

    public ConfigSchema(Template template) {
        flowControllerProperties = new FlowControllerSchema(template.getDetails());
        coreProperties = new CorePropertiesSchema();
        flowfileRepositoryProperties = new FlowFileRepositorySchema();
        contentRepositoryProperties = new ContentRepositorySchema();
        componentStatusRepositoryProperties = new ComponentStatusRepositorySchema();
        securityProperties = new SecurityPropertiesSchema();

        TemplateDTO templateDTO = template.getDetails();
        FlowSnippetDTO templateDTOSnippet = templateDTO.getSnippet();

        this.processors = nullToEmpty(templateDTOSnippet.getProcessors()).stream()
                .map(ProcessorSchema::new)
                .sorted(Comparator.comparing(ProcessorSchema::getName))
                .collect(Collectors.toList());

        this.connections = nullToEmpty(templateDTOSnippet.getConnections()).stream()
                .map(ConnectionSchema::new)
                .sorted(Comparator.comparing(ConnectionSchema::getName))
                .collect(Collectors.toList());

        this.remoteProcessingGroups = nullToEmpty(templateDTOSnippet.getRemoteProcessGroups()).stream()
                .map(RemoteProcessingGroupSchema::new)
                .sorted(Comparator.comparing(RemoteProcessingGroupSchema::getName))
                .collect(Collectors.toList());

        provenanceReportingProperties = new ProvenanceReportingSchema();
        provenanceRepositorySchema = new ProvenanceRepositorySchema();
    }

    public ConfigSchema(Map map) {
        flowControllerProperties = getMapAsType(map, FLOW_CONTROLLER_PROPS_KEY, FlowControllerSchema.class, TOP_LEVEL_NAME, true);

        coreProperties = getMapAsType(map, CORE_PROPS_KEY, CorePropertiesSchema.class, TOP_LEVEL_NAME, false);
        flowfileRepositoryProperties = getMapAsType(map, FLOWFILE_REPO_KEY, FlowFileRepositorySchema.class, TOP_LEVEL_NAME, false);
        contentRepositoryProperties = getMapAsType(map, CONTENT_REPO_KEY, ContentRepositorySchema.class, TOP_LEVEL_NAME, false);
        provenanceRepositorySchema = getMapAsType(map, PROVENANCE_REPO_KEY, ProvenanceRepositorySchema.class, TOP_LEVEL_NAME, false);
        componentStatusRepositoryProperties = getMapAsType(map, COMPONENT_STATUS_REPO_KEY, ComponentStatusRepositorySchema.class, TOP_LEVEL_NAME, false);
        securityProperties = getMapAsType(map, SECURITY_PROPS_KEY, SecurityPropertiesSchema.class, TOP_LEVEL_NAME, false);

        processors = getOptionalKeyAsType(map, PROCESSORS_KEY, List.class, TOP_LEVEL_NAME, null);
        if (processors != null) {
            transformListToType(processors, "processor", ProcessorSchema.class, PROCESSORS_KEY);
        }

        connections = getOptionalKeyAsType(map, CONNECTIONS_KEY, List.class, TOP_LEVEL_NAME, null);
        if (connections != null) {
            transformListToType(connections, "connection", ConnectionSchema.class, CONNECTIONS_KEY);
        }

        remoteProcessingGroups = getOptionalKeyAsType(map, REMOTE_PROCESSING_GROUPS_KEY, List.class, TOP_LEVEL_NAME, null);
        if (remoteProcessingGroups != null) {
            transformListToType(remoteProcessingGroups, "remote processing group", RemoteProcessingGroupSchema.class, REMOTE_PROCESSING_GROUPS_KEY);
        }

        provenanceReportingProperties = getMapAsType(map, PROVENANCE_REPORTING_KEY, ProvenanceReportingSchema.class, TOP_LEVEL_NAME, false, false);

        addIssuesIfNotNull(flowControllerProperties);
        addIssuesIfNotNull(coreProperties);
        addIssuesIfNotNull(flowfileRepositoryProperties);
        addIssuesIfNotNull(contentRepositoryProperties);
        addIssuesIfNotNull(componentStatusRepositoryProperties);
        addIssuesIfNotNull(securityProperties);
        addIssuesIfNotNull(provenanceReportingProperties);
        addIssuesIfNotNull(provenanceRepositorySchema);

        if (processors != null) {
            for (ProcessorSchema processorSchema : processors) {
                addIssuesIfNotNull(processorSchema);
            }
        }

        if (connections != null) {
            for (ConnectionSchema connectionSchema : connections) {
                addIssuesIfNotNull(connectionSchema);
            }
        }

        if (remoteProcessingGroups != null) {
            for (RemoteProcessingGroupSchema remoteProcessingGroupSchema : remoteProcessingGroups) {
                addIssuesIfNotNull(remoteProcessingGroupSchema);
            }
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = super.toMap();
        result.put(FLOW_CONTROLLER_PROPS_KEY, flowControllerProperties.toMap());
        putIfNotNull(result, CORE_PROPS_KEY, coreProperties);
        putIfNotNull(result, FLOWFILE_REPO_KEY, flowfileRepositoryProperties);
        putIfNotNull(result, CONTENT_REPO_KEY, contentRepositoryProperties);
        putIfNotNull(result, PROVENANCE_REPO_KEY, provenanceRepositorySchema);
        putIfNotNull(result, COMPONENT_STATUS_REPO_KEY, componentStatusRepositoryProperties);
        putIfNotNull(result, SECURITY_PROPS_KEY, securityProperties);
        putListIfNotNull(result, PROCESSORS_KEY, processors);
        putListIfNotNull(result, CONNECTIONS_KEY, connections);
        putListIfNotNull(result, REMOTE_PROCESSING_GROUPS_KEY, remoteProcessingGroups);
        putIfNotNull(result, PROVENANCE_REPORTING_KEY, provenanceReportingProperties);
        return result;
    }

    public FlowControllerSchema getFlowControllerProperties() {
        return flowControllerProperties;
    }

    public CorePropertiesSchema getCoreProperties() {
        return coreProperties;
    }

    public FlowFileRepositorySchema getFlowfileRepositoryProperties() {
        return flowfileRepositoryProperties;
    }

    public ContentRepositorySchema getContentRepositoryProperties() {
        return contentRepositoryProperties;
    }

    public SecurityPropertiesSchema getSecurityProperties() {
        return securityProperties;
    }

    public List<ProcessorSchema> getProcessors() {
        return processors;
    }

    public List<ConnectionSchema> getConnections() {
        return connections;
    }

    public List<RemoteProcessingGroupSchema> getRemoteProcessingGroups() {
        return remoteProcessingGroups;
    }

    public ProvenanceReportingSchema getProvenanceReportingProperties() {
        return provenanceReportingProperties;
    }

    public ComponentStatusRepositorySchema getComponentStatusRepositoryProperties() {
        return componentStatusRepositoryProperties;
    }

    public ProvenanceRepositorySchema getProvenanceRepositorySchema() {
        return provenanceRepositorySchema;
    }
}
