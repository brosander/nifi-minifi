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

import org.apache.nifi.minifi.commons.schema.SwapSchema;
import org.apache.nifi.minifi.commons.schema.avro.generated.Swap;

import java.util.Map;

public class SwapSchemaTranslator extends BaseAvroTranslator<Swap, Swap.Builder, SwapSchema> {
    public SwapSchemaTranslator() {
        super(SwapSchema::new, Swap::newBuilder);
    }

    @Override
    protected void putToMap(Map<String, Object> map, Swap avro) {
        putIfSet(map, SwapSchema.THRESHOLD_KEY, avro.getThreshold());
        putIfSet(map, SwapSchema.IN_PERIOD_KEY, avro.getInPeriod());
        putIfSet(map, SwapSchema.IN_THREADS_KEY, avro.getInThreads());
        putIfSet(map, SwapSchema.OUT_PERIOD_KEY, avro.getOutPeriod());
        putIfSet(map, SwapSchema.OUT_THREADS_KEY, avro.getOutThreads());
    }

    @Override
    protected void setOnBuilder(Swap.Builder builder, SwapSchema schema) {
        builder.setThreshold(schema.getThreshold());
        builder.setInPeriod(schema.getInPeriod());
        builder.setInThreads(schema.getInThreads());
        builder.setOutPeriod(schema.getOutPeriod());
        builder.setOutThreads(schema.getOutThreads());
    }
}
