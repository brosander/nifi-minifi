/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.apache.nifi.minifi.commons.schema.common;

import java.util.Map;

import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.NAME_KEY;

public abstract class BaseSchemaWithIdAndName extends BaseSchemaWithId {
    private String name;

    public BaseSchemaWithIdAndName(Map map, String wrapperName) {
        super(map, wrapperName);
        name = getName(map, wrapperName);
    }

    protected String getName(Map map, String wrapperName) {
        return getOptionalKeyAsType(map, NAME_KEY, String.class, wrapperName, "");
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = mapSupplier.get();
        map.put(NAME_KEY, name);
        map.putAll(super.toMap());
        return map;
    }
}
