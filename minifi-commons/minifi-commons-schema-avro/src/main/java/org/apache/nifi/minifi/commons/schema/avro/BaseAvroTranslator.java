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

import org.apache.avro.data.RecordBuilder;
import org.apache.avro.specific.SpecificRecord;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class BaseAvroTranslator<A extends SpecificRecord, AB extends RecordBuilder<A>, S> {
    private final Function<Map, S> schemaNew;
    private final Supplier<AB> builderSupplier;

    public BaseAvroTranslator(Function<Map, S> schemaNew, Supplier<AB> builderSupplier) {
        this.schemaNew = schemaNew;
        this.builderSupplier = builderSupplier;
    }

    public A toAvro(S schema) {
        AB builder = builderSupplier.get();
        setOnBuilder(builder, schema);
        return builder.build();
    }

    public S toSchema(A avro) {
        return schemaNew.apply(toMap(avro));
    }

    public Map<String, Object> toMap(A avro) {
        Map<String, Object> map = new HashMap<>();
        putToMap(map, avro);
        return map;
    }

    protected abstract void putToMap(Map<String, Object> map, A avro);

    protected abstract void setOnBuilder(AB builder, S schema);

    protected void putIfSet(Map<String, Object> map, String key, CharSequence charSequence) {
        putIfSet(map, key, charSequence, CharSequence::toString);
    }

    protected void putIfSet(Map<String, Object> map, String key, Object object) {
        putIfSet(map, key, object, Function.identity());
    }

    protected <InputType, TypeToPut> void putIfSet(Map<String, Object> map, String key, InputType value, Function<InputType, TypeToPut> converter) {
        if(value != null) {
            map.put(key, converter.apply(value));
        }
    }

    protected Map<String, Object> toStringMap(Map<CharSequence, CharSequence> charSequenceMap) {
        return charSequenceMap.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString()));
    }

    protected Map<CharSequence, CharSequence> toCharSequenceMap(Map<String, Object> charSequenceMap) {
        return charSequenceMap.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString()));
    }
}
