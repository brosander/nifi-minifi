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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SchemaDefinition extends BaseDefinitionWithImports {
    private static final String[] defaultImports = new String[] {
            Number.class.getCanonicalName(),
            String.class.getCanonicalName(),
            Boolean.class.getCanonicalName(),
            Map.class.getCanonicalName(),
            List.class.getCanonicalName(),
            "org.apache.nifi.minifi.commons.schema.common.BaseSchema",
            "org.apache.nifi.minifi.commons.schema.common.BaseSchemaWithId",
            "org.apache.nifi.minifi.commons.schema.common.BaseSchemaWithIdAndName",
            "org.apache.nifi.minifi.commons.schema.common.StringUtil",
            "org.apache.nifi.minifi.commons.schema.common.WritableSchema"
    };
    private Map<String, ClassDefinition> classes;

    public SchemaDefinition() {
        setImports(Collections.emptyList());
    }

    @Override
    public void setImports(Collection<CanonicalName> imports) {
        Set<CanonicalName> enrichedImports = new HashSet<>(imports);
        for (String s : defaultImports) {
            enrichedImports.add(new CanonicalName(s));
        }
        super.setImports(enrichedImports);
    }

    public List<ClassDefinition> getClasses() {
        return new ArrayList<>(classes.values());
    }

    public ClassDefinition getClass(String name) {
        return classes.get(name);
    }

    public void setClasses(List<ClassDefinition> classes) {
        LinkedHashMap<String, ClassDefinition> map = new LinkedHashMap<>();
        for (ClassDefinition classDefinition : classes) {
            map.put(classDefinition.getName(), classDefinition);
        }
        this.classes = map;
    }

    @Override
    public CanonicalName getCanonicalName(String name) {
        CanonicalName canonicalName = super.getCanonicalName(name);
        if (canonicalName == null) {
            ClassDefinition aClass = getClass(name);
            if (aClass != null) {
                canonicalName = new CanonicalName(aClass.getPackage() + "." + aClass.getName());
            }
        }
        return canonicalName;
    }
}
