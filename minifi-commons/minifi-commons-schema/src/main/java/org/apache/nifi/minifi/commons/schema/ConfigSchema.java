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

import org.apache.nifi.minifi.commons.schema.common.CollectionOverlap;
import org.apache.nifi.minifi.commons.schema.common.ConvertableSchema;
import org.apache.nifi.minifi.commons.schema.common.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigSchema extends AbstractConfigSchema implements ConvertableSchema<ConfigSchema> {
    public static final int CONFIG_VERSION = 3;
    public static final String VERSION = "MiNiFi Config Version";
    public static final String FOUND_THE_FOLLOWING_DUPLICATE_REMOTE_INPUT_PORT_IDS = "Found the following duplicate remote input port ids: ";
    public static final String FOUND_THE_FOLLOWING_DUPLICATE_REMOTE_OUTPUT_PORT_IDS = "Found the following duplicate remote output port ids: ";
    public static final String FOUND_THE_FOLLOWING_DUPLICATE_INPUT_PORT_IDS = "Found the following duplicate input port ids: ";
    public static final String FOUND_THE_FOLLOWING_DUPLICATE_OUTPUT_PORT_IDS = "Found the following duplicate output port ids: ";
    public static final String FOUND_THE_FOLLOWING_DUPLICATE_IDS = "Found the following ids that occur both in more than one Processor(s), Input Port(s), Output Port(s) and/or Remote Input Port(s): ";
    public static final String CONNECTION_WITH_ID = "Connection with id ";
    public static final String HAS_INVALID_SOURCE_ID = " has invalid source id ";
    public static final String HAS_INVALID_DESTINATION_ID = " has invalid destination id ";
    public static final String FOUND_THE_FOLLOWING_DUPLICATE_PROCESSOR_IDS = "Found the following duplicate processor ids: ";
    public static final String FOUND_THE_FOLLOWING_DUPLICATE_CONTROLLER_SERVICE_IDS = "Found the following duplicate controller service ids: ";
    public static final String FOUND_THE_FOLLOWING_DUPLICATE_CONNECTION_IDS = "Found the following duplicate connection ids: ";
    public static final String FOUND_THE_FOLLOWING_DUPLICATE_FUNNEL_IDS = "Found the following duplicate funnel ids: ";
    public static final String FOUND_THE_FOLLOWING_DUPLICATE_REMOTE_PROCESS_GROUP_NAMES = "Found the following duplicate remote process group names: ";

    public ConfigSchema(Map map) {
        this(map, Collections.emptyList());
    }

    public ConfigSchema(Map map, List<String> validationIssues) {
        super(map);
        validationIssues.stream().forEach(this::addValidationIssue);

        List<ProcessGroupSchema> allProcessGroups = getAllProcessGroups(getProcessGroupSchema());
        List<ConnectionSchema> allConnectionSchemas = allProcessGroups.stream().flatMap(p -> p.getConnections().stream()).collect(Collectors.toList());
        List<RemoteProcessGroupSchema> allRemoteProcessGroups = allProcessGroups.stream().flatMap(p -> p.getRemoteProcessGroups().stream()).collect(Collectors.toList());

        List<String> allProcessorIds = allProcessGroups.stream().flatMap(p -> p.getProcessors().stream()).map(ProcessorSchema::getId).collect(Collectors.toList());
        List<String> allControllerServiceIds = allProcessGroups.stream().flatMap(p -> p.getControllerServices().stream()).map(ControllerServiceSchema::getId).collect(Collectors.toList());
        List<String> allFunnelIds = allProcessGroups.stream().flatMap(p -> p.getFunnels().stream()).map(FunnelSchema::getId).collect(Collectors.toList());
        List<String> allConnectionIds = allConnectionSchemas.stream().map(ConnectionSchema::getId).collect(Collectors.toList());
        List<String> allRemoteProcessGroupNames = allRemoteProcessGroups.stream().map(RemoteProcessGroupSchema::getName).collect(Collectors.toList());
        List<String> allRemoteInputPortIds = allRemoteProcessGroups.stream().filter(r -> r.getInputPorts() != null)
                .flatMap(r -> r.getInputPorts().stream()).map(RemotePortSchema::getId).collect(Collectors.toList());
        List<String> allRemoteOutputPortIds = allRemoteProcessGroups.stream().filter(r -> r.getOutputPorts() != null)
                .flatMap(r -> r.getOutputPorts().stream()).map(RemotePortSchema::getId).collect(Collectors.toList());
        List<String> allInputPortIds = allProcessGroups.stream().flatMap(p -> p.getInputPortSchemas().stream()).map(PortSchema::getId).collect(Collectors.toList());
        List<String> allOutputPortIds = allProcessGroups.stream().flatMap(p -> p.getOutputPortSchemas().stream()).map(PortSchema::getId).collect(Collectors.toList());

        checkForDuplicates(this::addValidationIssue, FOUND_THE_FOLLOWING_DUPLICATE_PROCESSOR_IDS, allProcessorIds);
        checkForDuplicates(this::addValidationIssue, FOUND_THE_FOLLOWING_DUPLICATE_CONTROLLER_SERVICE_IDS, allControllerServiceIds);
        checkForDuplicates(this::addValidationIssue, FOUND_THE_FOLLOWING_DUPLICATE_FUNNEL_IDS, allFunnelIds);
        checkForDuplicates(this::addValidationIssue, FOUND_THE_FOLLOWING_DUPLICATE_CONNECTION_IDS, allConnectionIds);
        checkForDuplicates(this::addValidationIssue, FOUND_THE_FOLLOWING_DUPLICATE_REMOTE_PROCESS_GROUP_NAMES, allRemoteProcessGroupNames);
        checkForDuplicates(this::addValidationIssue, FOUND_THE_FOLLOWING_DUPLICATE_REMOTE_INPUT_PORT_IDS, allRemoteInputPortIds);
        checkForDuplicates(this::addValidationIssue, FOUND_THE_FOLLOWING_DUPLICATE_REMOTE_OUTPUT_PORT_IDS, allRemoteOutputPortIds);
        checkForDuplicates(this::addValidationIssue, FOUND_THE_FOLLOWING_DUPLICATE_INPUT_PORT_IDS, allInputPortIds);
        checkForDuplicates(this::addValidationIssue, FOUND_THE_FOLLOWING_DUPLICATE_OUTPUT_PORT_IDS, allOutputPortIds);

        // Potential connection sources and destinations need to have unique ids
        CollectionOverlap<String> overlapResults = new CollectionOverlap<>(new HashSet<>(allProcessorIds), new HashSet<>(allRemoteInputPortIds), new HashSet<>(allRemoteOutputPortIds),
                new HashSet<>(allInputPortIds), new HashSet<>(allOutputPortIds), new HashSet<>(allFunnelIds));
        if (overlapResults.getDuplicates().size() > 0) {
            addValidationIssue(FOUND_THE_FOLLOWING_DUPLICATE_IDS + overlapResults.getDuplicates().stream().sorted().collect(Collectors.joining(", ")));
        }

        allConnectionSchemas.forEach(c -> {
            String destinationId = c.getDestinationId();
            if (!StringUtil.isNullOrEmpty(destinationId) && !overlapResults.getElements().contains(destinationId)) {
                addValidationIssue(CONNECTION_WITH_ID + c.getId() + HAS_INVALID_DESTINATION_ID + destinationId);
            }
            String sourceId = c.getSourceId();
            if (!StringUtil.isNullOrEmpty(sourceId) && !overlapResults.getElements().contains(sourceId)) {
                addValidationIssue(CONNECTION_WITH_ID + c.getId() + HAS_INVALID_SOURCE_ID + sourceId);
            }
        });
    }

    public static List<ProcessGroupSchema> getAllProcessGroups(ProcessGroupSchema processGroupSchema) {
        List<ProcessGroupSchema> result = new ArrayList<>();
        addProcessGroups(processGroupSchema, result);
        return result;
    }

    private static void addProcessGroups(ProcessGroupSchema processGroupSchema, List<ProcessGroupSchema> result) {
        result.add(processGroupSchema);
        processGroupSchema.getProcessGroupSchemas().forEach(p -> addProcessGroups(p, result));
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> result = mapSupplier.get();
        result.put(VERSION, getVersion());
        result.putAll(super.toMap());
        return result;
    }

    @Override
    public int getVersion() {
        return CONFIG_VERSION;
    }

    @Override
    public ConfigSchema convert() {
        return this;
    }
}
