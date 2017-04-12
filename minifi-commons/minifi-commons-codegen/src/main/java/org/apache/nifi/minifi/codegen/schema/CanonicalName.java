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

public class CanonicalName {
    private final String packageName;
    private final String name;

    public CanonicalName(String canonicalName) {
        int lastDot = canonicalName.lastIndexOf('.');
        if (lastDot >= 0) {
            this.packageName = canonicalName.substring(0, lastDot);
            this.name = canonicalName.substring(lastDot + 1);
        } else {
            this.packageName = "";
            this.name = canonicalName;
        }
    }

    public String getPackage() {
        return packageName;
    }

    public String getName() {
        return name;
    }

    public String getCanonicalName() {
        return packageName + "." + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CanonicalName that = (CanonicalName) o;

        if (!packageName.equals(that.packageName)) return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = packageName.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
