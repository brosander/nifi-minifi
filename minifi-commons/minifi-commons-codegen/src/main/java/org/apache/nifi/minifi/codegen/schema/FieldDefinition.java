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
    private ClassDefinition parent;
    private String name;
    private String key;
    private String type;
    private Object defaultValue;
    private boolean required;
    private boolean isEnum;
    private boolean requiredSet;
    private String validator;
    private String defaultType;

    public void setParent(ClassDefinition parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultLiteral() {
        if (String.class.getSimpleName().equals(type)) {
            return "\"" + String.valueOf(defaultValue) + "\"";
        }
        return String.valueOf(defaultValue);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMapType() {
        if ("int".equals(type) || "long".equals(type)) {
            return "Number";
        }
        if ("boolean".equals(type)) {
            return "Boolean";
        }

        int i = type.indexOf('<');
        if (i < 0) {
            return type;
        }
        return type.substring(0, i);
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
        this.defaultValue = defaultValue;
    }

    public boolean hasDefault() {
        return defaultValue != null;
    }

    public boolean isRequired() {
        if (requiredSet) {
            return required;
        } else {
            return !hasDefault();
        }
    }

    public void setRequired(boolean required) {
        requiredSet = true;
        this.required = required;
    }

    public boolean isEnum() {
        return isEnum;
    }

    public void setEnum(boolean anEnum) {
        isEnum = anEnum;
    }

    public String getValidator() {
        return validator;
    }

    public void setValidator(String validator) {
        this.validator = validator;
    }
}
