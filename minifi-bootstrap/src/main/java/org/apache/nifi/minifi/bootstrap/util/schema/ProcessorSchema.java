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
import org.apache.nifi.scheduling.SchedulingStrategy;
import org.apache.nifi.web.api.dto.ProcessorConfigDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.RelationshipDTO;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.apache.nifi.minifi.bootstrap.util.schema.common.CommonPropertyKeys.MAX_CONCURRENT_TASKS_KEY;
import static org.apache.nifi.minifi.bootstrap.util.schema.common.CommonPropertyKeys.NAME_KEY;
import static org.apache.nifi.minifi.bootstrap.util.schema.common.CommonPropertyKeys.PROCESSORS_KEY;
import static org.apache.nifi.minifi.bootstrap.util.schema.common.CommonPropertyKeys.SCHEDULING_PERIOD_KEY;
import static org.apache.nifi.minifi.bootstrap.util.schema.common.CommonPropertyKeys.SCHEDULING_STRATEGY_KEY;
import static org.apache.nifi.minifi.bootstrap.util.schema.common.CommonPropertyKeys.YIELD_PERIOD_KEY;

/**
 *
 */
public class ProcessorSchema extends BaseSchema {
    public static final String CLASS_KEY = "class";
    public static final String PENALIZATION_PERIOD_KEY = "penalization period";
    public static final String RUN_DURATION_NANOS_KEY = "run duration nanos";
    public static final String AUTO_TERMINATED_RELATIONSHIPS_LIST_KEY = "auto-terminated relationships list";
    public static final String PROCESSOR_PROPS_KEY = "Properties";


    private String name;
    private String processorClass;
    private Number maxConcurrentTasks = 1;
    private String schedulingStrategy;
    private String schedulingPeriod;
    private String penalizationPeriod = "30 sec";
    private String yieldPeriod = "1 sec";
    private Number runDurationNanos = 0;
    private List<String> autoTerminatedRelationshipsList = Collections.emptyList();
    private Map<String, Object> properties = Collections.emptyMap();

    public ProcessorSchema() {
    }

    public ProcessorSchema(ProcessorDTO processorDTO) {
        ProcessorConfigDTO processorDTOConfig = processorDTO.getConfig();

        this.name = processorDTO.getName();
        this.processorClass = processorDTO.getType();
        this.maxConcurrentTasks = processorDTOConfig.getConcurrentlySchedulableTaskCount();
        this.schedulingStrategy = processorDTOConfig.getSchedulingStrategy();
        this.schedulingPeriod = processorDTOConfig.getSchedulingPeriod();
        this.penalizationPeriod = processorDTOConfig.getPenaltyDuration();
        this.yieldPeriod = processorDTOConfig.getYieldDuration();
        this.runDurationNanos = processorDTOConfig.getRunDurationMillis() * 1000;
        this.autoTerminatedRelationshipsList = nullToEmpty(processorDTO.getRelationships()).stream()
                .filter(RelationshipDTO::isAutoTerminate)
                .map(RelationshipDTO::getName)
                .collect(Collectors.toList());
        this.properties = new HashMap<>(nullToEmpty(processorDTOConfig.getProperties()));
    }

    public ProcessorSchema(Map map) {
        name = getRequiredKeyAsType(map, NAME_KEY, String.class, PROCESSORS_KEY);
        processorClass = getRequiredKeyAsType(map, CLASS_KEY, String.class, PROCESSORS_KEY);

        maxConcurrentTasks = getOptionalKeyAsType(map, MAX_CONCURRENT_TASKS_KEY, Number.class, PROCESSORS_KEY, 1);

        schedulingStrategy = getRequiredKeyAsType(map, SCHEDULING_STRATEGY_KEY, String.class, PROCESSORS_KEY);
        try {
            SchedulingStrategy.valueOf(schedulingStrategy);
        } catch (IllegalArgumentException e) {
            addValidationIssue(SCHEDULING_STRATEGY_KEY, PROCESSORS_KEY, "it is not a valid scheduling strategy");
        }

        schedulingPeriod = getRequiredKeyAsType(map, SCHEDULING_PERIOD_KEY, String.class, PROCESSORS_KEY);

        penalizationPeriod = getOptionalKeyAsType(map, PENALIZATION_PERIOD_KEY, String.class, PROCESSORS_KEY, "30 sec");

        yieldPeriod = getOptionalKeyAsType(map, YIELD_PERIOD_KEY, String.class, PROCESSORS_KEY, "1 sec");

        runDurationNanos = getOptionalKeyAsType(map, RUN_DURATION_NANOS_KEY, Number.class, PROCESSORS_KEY, 0);

        autoTerminatedRelationshipsList = getOptionalKeyAsType(map, AUTO_TERMINATED_RELATIONSHIPS_LIST_KEY, List.class, PROCESSORS_KEY, null);

        properties = getOptionalKeyAsType(map, PROCESSOR_PROPS_KEY, Map.class, PROCESSORS_KEY, null);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> result = super.toMap();
        result.put(NAME_KEY, name);
        result.put(CLASS_KEY, processorClass);
        result.put(MAX_CONCURRENT_TASKS_KEY, maxConcurrentTasks);
        result.put(SCHEDULING_STRATEGY_KEY, schedulingStrategy);
        result.put(SCHEDULING_PERIOD_KEY, schedulingPeriod);
        result.put(PENALIZATION_PERIOD_KEY, penalizationPeriod);
        result.put(YIELD_PERIOD_KEY, yieldPeriod);
        result.put(RUN_DURATION_NANOS_KEY, runDurationNanos);
        result.put(AUTO_TERMINATED_RELATIONSHIPS_LIST_KEY, autoTerminatedRelationshipsList);
        result.put(PROCESSOR_PROPS_KEY, new TreeMap<>(properties));
        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProcessorClass() {
        return processorClass;
    }

    public Number getMaxConcurrentTasks() {
        return maxConcurrentTasks;
    }

    public String getSchedulingStrategy() {
        return schedulingStrategy;
    }

    public String getSchedulingPeriod() {
        return schedulingPeriod;
    }

    public String getPenalizationPeriod() {
        return penalizationPeriod;
    }

    public String getYieldPeriod() {
        return yieldPeriod;
    }

    public Number getRunDurationNanos() {
        return runDurationNanos;
    }

    public List<String> getAutoTerminatedRelationshipsList() {
        return autoTerminatedRelationshipsList;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
