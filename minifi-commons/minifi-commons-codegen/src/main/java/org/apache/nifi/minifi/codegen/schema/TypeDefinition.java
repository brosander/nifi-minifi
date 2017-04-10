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

import java.util.Collections;
import java.util.List;

public class TypeDefinition {
    private String name;
    private boolean isEnum;
    private List<TypeDefinition> genericTypes = Collections.emptyList();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnum() {
        return isEnum;
    }

    public void setEnum(boolean anEnum) {
        isEnum = anEnum;
    }

    public List<TypeDefinition> getGenericTypes() {
        return genericTypes;
    }

    public void setGenericTypes(List<TypeDefinition> genericTypes) {
        this.genericTypes = genericTypes;
    }

    public boolean isPrimitive() {
        return name.equals(name.toLowerCase());
    }

    public String getMapType() {
        if ("int".equals(name) || "long".equals(name)) {
            return "Number";
        }
        if ("boolean".equals(name)) {
            return "Boolean";
        }
        return name;
    }

    public String getDeclaration() {
        if (getGenericTypes().size() == 0) {
            return name;
        }
        StringBuilder sb = new StringBuilder(name).append("<");
        for (TypeDefinition typeDefinition : getGenericTypes()) {
            sb.append(typeDefinition.getDeclaration());
            sb.append(", ");
        }
        sb.setLength(sb.length() - 2);
        return sb.append(">").toString();
    }
}
