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

public class FieldDefinition {
    private String name;
    private String key;
    private TypeDefinition type;
    private Object defaultValue;
    private boolean defaultSet;
    private boolean required;
    private boolean requiredSet;
    private String validator;
    private String instantiator;
    private String toMap;
    private boolean omitIfEmpty;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TypeDefinition getType() {
        return type;
    }

    public void setTypeName(String type) {
        TypeDefinition typeDefinition = new TypeDefinition();
        typeDefinition.setName(type);
        this.type = typeDefinition;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getDefault() {
        return defaultValue;
    }

    public void setDefault(Object defaultValue) {
        this.defaultSet = true;
        this.defaultValue = defaultValue;
    }

    public boolean hasDefault() {
        return defaultValue != null || defaultSet;
    }

    public boolean isRequired() {
        if (requiredSet) {
            return required;
        }
        return !hasDefault();
    }

    public void setRequired(boolean required) {
        requiredSet = true;
        this.required = required;
    }

    public String getValidator() {
        return validator;
    }

    public void setValidator(String validator) {
        this.validator = validator;
    }

    public void setType(TypeDefinition type) {
        this.type = type;
    }

    public String getInstantiator() {
        return instantiator;
    }

    public void setInstantiator(String instantiator) {
        this.instantiator = instantiator;
    }

    public String getToMap() {
        return toMap;
    }

    public void setToMap(String toMap) {
        this.toMap = toMap;
    }

    public boolean isOmitIfEmpty() {
        return omitIfEmpty;
    }

    public void setOmitIfEmpty(boolean omitIfEmpty) {
        this.omitIfEmpty = omitIfEmpty;
    }
}
