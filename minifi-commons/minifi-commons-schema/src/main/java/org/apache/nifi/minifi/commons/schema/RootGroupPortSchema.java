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

package org.apache.nifi.minifi.commons.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.nifi.minifi.commons.schema.ProcessorSchema.DEFAULT_MAX_CONCURRENT_TASKS;
import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.MAX_CONCURRENT_TASKS_KEY;

public class RootGroupPortSchema extends PortSchema {
    public static final String USER_ACCESS_CONTROL_KEY = "user access control";
    public static final String GROUP_ACCESS_CONTROL_KEY = "group access control";

    private Number maxConcurrentTasks = DEFAULT_MAX_CONCURRENT_TASKS;
    private List<String> userAccessControl = new ArrayList<>();
    private List<String> groupAccessControl = new ArrayList<>();

    public RootGroupPortSchema(Map map, String wrapperName) {
        super(map, wrapperName);
        maxConcurrentTasks = getOptionalKeyAsType(map, MAX_CONCURRENT_TASKS_KEY, Number.class, wrapperName, DEFAULT_MAX_CONCURRENT_TASKS);
        userAccessControl = getOptionalKeyAsList(map, USER_ACCESS_CONTROL_KEY, String::valueOf, wrapperName);
        groupAccessControl = getOptionalKeyAsList(map, GROUP_ACCESS_CONTROL_KEY, String::valueOf, wrapperName);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put(MAX_CONCURRENT_TASKS_KEY, maxConcurrentTasks);
        map.put(USER_ACCESS_CONTROL_KEY, userAccessControl);
        map.put(GROUP_ACCESS_CONTROL_KEY, groupAccessControl);
        return map;
    }

    public Number getMaxConcurrentTasks() {
        return maxConcurrentTasks;
    }

    public List<String> getUserAccessControl() {
        return userAccessControl;
    }

    public List<String> getGroupAccessControl() {
        return groupAccessControl;
    }
}
