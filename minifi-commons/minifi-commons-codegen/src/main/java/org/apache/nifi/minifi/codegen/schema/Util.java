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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.HashMap;
import java.util.Map;

public class Util {
    private final LoadingCache<String, String> underscoreCache = CacheBuilder.newBuilder().build(new CacheLoader<String, String>() {
        @Override
        public String load(String name) throws Exception {
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
    });

    private final LoadingCache<String, String> camelCaseCache = CacheBuilder.newBuilder().build(new CacheLoader<String, String>() {
        @Override
        public String load(String name) throws Exception {
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
    });

    public Map<Object, Object> newMap() {
        return new HashMap<>();
    }

    public String underscore(String name) {
        return underscoreCache.getUnchecked(name);
    }

    public String camelCase(String name) {
        return camelCaseCache.getUnchecked(name);
    }
}
