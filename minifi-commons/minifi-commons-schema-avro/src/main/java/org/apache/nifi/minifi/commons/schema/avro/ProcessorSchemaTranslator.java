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

import org.apache.nifi.minifi.commons.schema.ProcessorSchema;
import org.apache.nifi.minifi.commons.schema.avro.generated.Processor;
import org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

public class ProcessorSchemaTranslator extends BaseAvroTranslator<Processor, Processor.Builder, ProcessorSchema> {
    public ProcessorSchemaTranslator() {
        super(ProcessorSchema::new, Processor::newBuilder);
    }

    @Override
    protected void putToMap(Map<String, Object> map, Processor avro) {
        putIfSet(map, CommonPropertyKeys.CLASS_KEY, avro.getProcessorClass());
        putIfSet(map, CommonPropertyKeys.SCHEDULING_STRATEGY_KEY, avro.getSchedulingStrategy());
        putIfSet(map, CommonPropertyKeys.SCHEDULING_PERIOD_KEY, avro.getSchedulingPeriod());
        putIfSet(map, CommonPropertyKeys.MAX_CONCURRENT_TASKS_KEY, avro.getMaxConcurrentTasks());
        putIfSet(map, ProcessorSchema.PENALIZATION_PERIOD_KEY, avro.getPenalizationPeriod());
        putIfSet(map, CommonPropertyKeys.YIELD_PERIOD_KEY, avro.getYieldPeriod());
        putIfSet(map, ProcessorSchema.RUN_DURATION_NANOS_KEY, avro.getRunDurationNanos());
        putIfSet(map, ProcessorSchema.AUTO_TERMINATED_RELATIONSHIPS_LIST_KEY, avro.getAutoTerminatedRelationshipsList(), r -> r.stream().map(CharSequence::toString).collect(Collectors.toList()));
        putIfSet(map, CommonPropertyKeys.PROPERTIES_KEY, avro.getProperties(), this::toStringMap);
        putIfSet(map, CommonPropertyKeys.ANNOTATION_DATA_KEY, avro.getAnnotationData());
    }

    @Override
    protected void setOnBuilder(Processor.Builder builder, ProcessorSchema schema) {
        builder.setProcessorClass(schema.getProcessorClass());
        builder.setSchedulingStrategy(schema.getSchedulingStrategy());
        builder.setSchedulingPeriod(schema.getSchedulingPeriod());
        builder.setMaxConcurrentTasks(schema.getMaxConcurrentTasks().intValue());
        builder.setPenalizationPeriod(schema.getPenalizationPeriod());
        builder.setYieldPeriod(schema.getYieldPeriod());
        builder.setRunDurationNanos(schema.getRunDurationNanos().intValue());
        builder.setAutoTerminatedRelationshipsList(new ArrayList<>(schema.getAutoTerminatedRelationshipsList()));
        builder.setProperties(toCharSequenceMap(schema.getProperties()));
        builder.setAnnotationData(schema.getAnnotationData());
    }
}
