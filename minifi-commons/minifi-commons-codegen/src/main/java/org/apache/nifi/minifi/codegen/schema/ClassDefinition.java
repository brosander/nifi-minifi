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
    private String wrapperName;
    private String name;
    private String extendsClass = "BaseSchema";
    private boolean writable;
    private boolean concrete = true;
    private Set<TypeDefinition> implementsClasses = Collections.emptySet();
    private List<FieldDefinition> fields = Collections.emptyList();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FieldDefinition> getFields() {
        return fields;
    }

    public void setImplements(Collection<TypeDefinition> implementsClasses) {
        this.implementsClasses = new HashSet<>(implementsClasses);
    }

    public String getImplementsString() {
        List<String> result = new ArrayList<>();
        if (writable) {
            result.add("WritableSchema");
        }
        for (FieldDefinition field : fields) {
            if ("String".equals(field.getType().getName()) && "id".equals(field.getName())) {
                    result.add("HasId");
            }
        }
        for (TypeDefinition implementsClass : implementsClasses) {
            result.add(implementsClass.getDeclaration());
        }
        return result.stream().sorted().collect(Collectors.joining(", "));
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

    public SchemaDefinition getParent() {
        return parent;
    }

    public String getWrapperName() {
        return wrapperName;
    }

    public void setWrapperName(String wrapperName) {
        this.wrapperName = wrapperName;
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
        if (writable) {
            imports.add(getCanonicalName("org.apache.nifi.minifi.commons.schema.common.WritableSchema"));
        }
        for (TypeDefinition type : implementsClasses) {
            addType(imports, type);
        }
        for (FieldDefinition field : fields) {
            addType(imports, field.getType());
            if ("String".equals(field.getType().getName())) {
                imports.add(getCanonicalName("org.apache.nifi.minifi.commons.schema.common.StringUtil"));
                if ("id".equals(field.getName())) {
                    imports.add(getCanonicalName("org.apache.nifi.minifi.commons.schema.common.HasId"));
                }
            }
        }
        imports.add(new CanonicalName(List.class.getCanonicalName()));
        imports.add(new CanonicalName(ArrayList.class.getCanonicalName()));
        imports.add(new CanonicalName(Collections.class.getCanonicalName()));
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

    private void addType(Set<CanonicalName> imports, TypeDefinition typeDefinition) {
        imports.add(getCanonicalName(typeDefinition.getMapType()));
        for (TypeDefinition definition : typeDefinition.getGenericTypes()) {
            addType(imports, definition);
        }
    }

    @Override
    public String getPackage() {
        String aPackage = super.getPackage();
        return aPackage == null ? parent.getPackage() : aPackage;
    }

    public boolean isConcrete() {
        return concrete;
    }

    public void setConcrete(boolean concrete) {
        this.concrete = concrete;
    }
}
