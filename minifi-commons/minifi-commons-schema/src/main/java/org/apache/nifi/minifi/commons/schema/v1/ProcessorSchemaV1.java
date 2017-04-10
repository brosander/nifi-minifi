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

package org.apache.nifi.minifi.commons.schema.v1;

import org.apache.nifi.minifi.commons.schema.ProcessorSchema;
import org.apache.nifi.minifi.commons.schema.common.ConvertableSchema;
import org.apache.nifi.scheduling.SchedulingStrategy;

import java.util.HashMap;
import java.util.Map;

import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.CLASS_KEY;
import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.PROCESSORS_KEY;

public class ProcessorSchemaV1 extends AbstractProcessorSchemaV1 implements ConvertableSchema<ProcessorSchema> {
    public static final String IT_IS_NOT_A_VALID_SCHEDULING_STRATEGY = "it is not a valid scheduling strategy";

    public ProcessorSchemaV1(Map map) {
        super(map);
        if (getSchedulingStrategy() != null && !isSchedulingStrategy(getSchedulingStrategy())) {
            addValidationIssue(SCHEDULING_STRATEGY_KEY, PROCESSORS_KEY, IT_IS_NOT_A_VALID_SCHEDULING_STRATEGY);
        }
    }

    @Override
    public ProcessorSchema convert() {
        Map<String, Object> map = new HashMap<>();
        map.put(NAME_KEY, getName());
        map.put(CLASS_KEY, getProcessorClass());
        map.put(MAX_CONCURRENT_TASKS_KEY, getMaxConcurrentTasks());
        map.put(SCHEDULING_STRATEGY_KEY, getSchedulingStrategy());
        map.put(SCHEDULING_PERIOD_KEY, getSchedulingPeriod());
        map.put(PENALIZATION_PERIOD_KEY, getPenalizationPeriod());
        map.put(YIELD_PERIOD_KEY, getYieldPeriod());
        map.put(RUN_DURATION_NANOS_KEY, getRunDurationNanos());
        map.put(AUTO_TERMINATED_RELATIONSHIPS_LIST_KEY, getAutoTerminatedRelationshipsList());
        map.put(PROPERTIES_KEY, new HashMap<>(getProperties()));
        return new ProcessorSchema(map);
    }

    @Override
    public int getVersion() {
        return ConfigSchemaV1.CONFIG_VERSION;
    }

    public static boolean isSchedulingStrategy(String string) {
        try {
            SchedulingStrategy.valueOf(string);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
