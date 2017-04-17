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

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SchemaUtil {
    public String getActualClassname(ClassDefinition classDefinition) {
        if (classDefinition.isConcrete()) {
            return classDefinition.getName();
        }
        return "Abstract" + classDefinition.getName();
    }

    public String getDefaultLiteral(FieldDefinition fieldDefinition) {
        TypeDefinition type = fieldDefinition.getType();
        Object defaultValue = fieldDefinition.getDefault();
        if (defaultValue != null) {
            if (String.class.getSimpleName().equals(type.getDeclaration()) && defaultValue != null) {
                return "\"" + String.valueOf(defaultValue) + "\"";
            }
            if (Map.class.getSimpleName().equals(type.getName())) {
                Map map = (Map) defaultValue;
                if (map.size() == 0) {
                    return "Collections.emptyMap()";
                }
            }
            if (List.class.getSimpleName().equals(type.getName())) {
                List list = (List) defaultValue;
                if (list.size() == 0) {
                    return "Collections.emptyList()";
                }
            }
        }
        return String.valueOf(defaultValue);
    }

    public String getWrapperNameCreator(ClassDefinition classDefinition) {
        String originalWrapperName = "\"" + classDefinition.getWrapperName() + "\"";
        String wrapperName = originalWrapperName;
        for (FieldDefinition fieldDefinition : classDefinition.getFields()) {
            wrapperName = wrapperName.replaceAll(Pattern.quote("{" + fieldDefinition.getName() + "}"), "\").append(" + fieldDefinition.getName() + ").append(\"");
        }
        if (originalWrapperName.equals(wrapperName)) {
            return originalWrapperName;
        }
        return "new StringBuilder(" + wrapperName + ").toString()";
    }
}
