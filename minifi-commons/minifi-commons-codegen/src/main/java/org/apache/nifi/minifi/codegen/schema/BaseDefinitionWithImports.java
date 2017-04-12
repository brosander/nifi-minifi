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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseDefinitionWithImports {
    private Map<String, CanonicalName> imports = new HashMap<>();
    private String packageName;

    public CanonicalName getCanonicalName(String name) {
        return imports.get(name);
    }

    public void setImports(Collection<CanonicalName> imports) {
        this.imports = new HashMap<>(imports.size());
        for (CanonicalName canonicalName : imports) {
            this.imports.put(canonicalName.getName(), canonicalName);
        }
    }

    public String getPackage() {
        return packageName;
    }

    public void setPackage(String packageName) {
        this.packageName = packageName;
    }
}
