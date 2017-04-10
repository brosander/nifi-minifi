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

import java.util.HashMap;
import java.util.Map;

public class Util {
    public Map<Object, Object> newMap() {
        return new HashMap<>();
    }

    public String calculateUnderscoreName(String name) {
        StringBuilder sb = new StringBuilder();
        char[] chars = name.toCharArray();
        char[] upperChars = name.toUpperCase().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i > 0 && chars[i] == upperChars[i]) {
                sb.append("_");
            }
            sb.append(upperChars[i]);
        }
        return sb.toString();
    }

    public String calculateGetterName(String name) {
        return "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}