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

package org.apache.nifi.minifi.codegen.schema;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvroUtil {
    private static final Map<String, String> avroTypeMap = initAvroTypeMap();

    private static Map<String, String> initAvroTypeMap() {
        Map<String, String> result = new HashMap<>();
        for (String s : Arrays.asList(Long.class.getSimpleName(), String.class.getSimpleName())) {
            result.put(s, "\"" + s.toLowerCase() + "\"");
        }
        result.put(Integer.class.getSimpleName(), "\"int\"");
        return result;
    }

    public String getAvroType(ClassDefinition classDefinition, TypeDefinition type) {
        String result = avroTypeMap.get(type.getName());
        if (result != null) {
            return result;
        }
        if (List.class.getSimpleName().equals(type.getName())) {
            return "{\"type\": \"array\", \"items\": " + getAvroType(classDefinition, type.getGenericTypes().get(0)) + "}";
        }
        if (Map.class.getSimpleName().equals(type.getName())) {
            return "{\"type\": \"map\", \"values\": " + getAvroType(classDefinition, type.getGenericTypes().get(1)) + "}";
        }
        if (type.isEnum()) {
            EnumDefinition enumDefinition = classDefinition.getParent().getEnum(type.getName());
            return "\"" + enumDefinition.getPackage() + ".avro." + enumDefinition.getName() + "\"";
        }
        if (classDefinition.isSchema(type.getName())) {
            CanonicalName canonicalName = classDefinition.getCanonicalName(type.getName());
            return "\"" + canonicalName.getPackage() + ".avro." + canonicalName.getName() + "\"";
        }
        return "\"" + type.getDeclaration() + "\"";
    }

    public String getDefaultLiteral(FieldDefinition fieldDefinition) {
        if ((String.class.getSimpleName().equals(fieldDefinition.getType().getDeclaration()) || fieldDefinition.getType().isEnum()) && fieldDefinition.getDefault() != null) {
            return "\"" + String.valueOf(fieldDefinition.getDefault()) + "\"";
        }
        return String.valueOf(fieldDefinition.getDefault());
    }
}
