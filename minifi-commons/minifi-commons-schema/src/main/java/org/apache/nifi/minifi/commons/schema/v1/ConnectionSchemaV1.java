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

package org.apache.nifi.minifi.commons.schema.v1;

import org.apache.nifi.minifi.commons.schema.ConnectionSchema;
import org.apache.nifi.minifi.commons.schema.common.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.apache.nifi.minifi.commons.schema.ConnectionSchema.SOURCE_RELATIONSHIP_NAMES_KEY;

public class ConnectionSchemaV1 extends AbstractConnectionSchemaV1 {
    public ConnectionSchemaV1(Map map) {
        super(map);
    }

    @Override
    public ConnectionSchema convert() {
        Map<String, Object> map = new HashMap<>();
        map.put(NAME_KEY, getName());
        if (StringUtil.isNullOrEmpty(getSourceRelationshipName())) {
            map.put(SOURCE_RELATIONSHIP_NAMES_KEY, new ArrayList<>());
        } else {
            map.put(SOURCE_RELATIONSHIP_NAMES_KEY, new ArrayList<>(Arrays.asList(getSourceRelationshipName())));
        }
        map.put(MAX_WORK_QUEUE_SIZE_KEY, getMaxWorkQueueSize());
        map.put(MAX_WORK_QUEUE_DATA_SIZE_KEY, getMaxWorkQueueDataSize());
        map.put(FLOWFILE_EXPIRATION_KEY, getFlowfileExpiration());
        map.put(QUEUE_PRIORITIZER_CLASS_KEY, getQueuePrioritizerClass());
        return new ConnectionSchema(map);
    }

    @Override
    public int getVersion() {
        return ConfigSchemaV1.CONFIG_VERSION;
    }
}
