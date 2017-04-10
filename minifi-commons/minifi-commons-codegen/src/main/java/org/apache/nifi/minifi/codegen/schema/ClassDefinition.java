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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ClassDefinition extends BaseDefinitionWithImports {
    private SchemaDefinition parent;
    private String key;
    private String name;
    private String extendsClass = "org.apache.nifi.minifi.commons.schema.common.BaseSchema";
    private boolean writable;
    private Set<String> implementsClasses = Collections.emptySet();
    private List<FieldDefinition> fields = Collections.emptyList();

    public ClassDefinition() {
        setPackage(null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FieldDefinition> getFields() {
        return fields;
    }

    public void setImplements(Collection<String> implementsClasses) {
        this.implementsClasses = new HashSet<>(implementsClasses);
    }

    public String getImplementsString() {
        Set<String> implementsClasses = getImplementsClasses();
        if (implementsClasses == null) return null;
        List<String> result = new ArrayList<>(implementsClasses.size());
        for (String implementsClass : implementsClasses) {
            result.add(getCanonicalName(implementsClass).getName());
        }
        Collections.sort(result);
        return result.stream().collect(Collectors.joining(", "));
    }

    public Set<String> getImplementsClasses() {
        Set<String> implementsClasses = new HashSet<>(this.implementsClasses);
        if (writable) {
            implementsClasses.add("org.apache.nifi.minifi.commons.schema.common.WritableSchema");
        }
        if (implementsClasses.size() == 0) {
            return null;
        }
        return implementsClasses;
    }

    public void setExtends(String extendsClass) {
        this.extendsClass = extendsClass;
    }

    public String getExtends() {
        return getCanonicalName(extendsClass).getName();
    }

    public void setFields(List<FieldDefinition> fields) {
        this.fields = fields;
    }

    public void setWritable(boolean writable) {
        this.writable = writable;
    }

    public boolean isWritable() {
        return writable;
    }

    public void setParent(SchemaDefinition parent) {
        this.parent = parent;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isSchema(String name) {
        return parent.getClass(name) != null;
    }

    @Override
    public CanonicalName getCanonicalName(String name) {
        CanonicalName result = super.getCanonicalName(name);
        if (result == null) {
            result = parent.getCanonicalName(name);
        }
        if (result == null) {
            result = new CanonicalName(name);
        }
        return result;
    }

    public List<String> getClassImports() {
        Set<CanonicalName> imports = new HashSet<>();
        if (extendsClass != null) {
            imports.add(getCanonicalName(extendsClass));
        }
        for (String implementsClass : getImplementsClasses()) {
            imports.add(getCanonicalName(implementsClass));
        }
        for (FieldDefinition field : fields) {
            imports.add(getCanonicalName(field.getType()));
        }
        imports.add(new CanonicalName(Map.class.getCanonicalName()));
        List<String> result = new ArrayList<>(imports.size());
        for (CanonicalName anImport : imports) {
            String aPackage = anImport.getPackage();
            if (!"java.lang".equals(aPackage) && !getPackage().equals(aPackage)) {
                result.add(anImport.getCanonicalName());
            }
        }
        Collections.sort(result);
        return result;
    }

    @Override
    public String getPackage() {
        String aPackage = super.getPackage();
        if (aPackage == null) {
            aPackage = parent.getPackage();
        }
        return aPackage;
    }
}
