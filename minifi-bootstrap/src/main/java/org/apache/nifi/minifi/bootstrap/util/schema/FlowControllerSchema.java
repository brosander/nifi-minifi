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
package org.apache.nifi.minifi.bootstrap.util.schema;

import org.apache.nifi.minifi.bootstrap.util.schema.common.BaseSchema;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;

import java.util.Map;

import static org.apache.nifi.minifi.bootstrap.util.schema.common.CommonPropertyKeys.COMMENT_KEY;
import static org.apache.nifi.minifi.bootstrap.util.schema.common.CommonPropertyKeys.FLOW_CONTROLLER_PROPS_KEY;
import static org.apache.nifi.minifi.bootstrap.util.schema.common.CommonPropertyKeys.NAME_KEY;

/**
 *
 */
public class FlowControllerSchema extends BaseSchema {
    public static final String DEFAULT_COMMENT = "";

    private String name;
    private String comment = DEFAULT_COMMENT;

    public FlowControllerSchema() {
    }

    public FlowControllerSchema(TemplateDTO templateDTO) {
        this.name = templateDTO.getName();
        this.comment = templateDTO.getDescription();
    }

    public FlowControllerSchema(Map map) {
        name = getRequiredKeyAsType(map, NAME_KEY, String.class, FLOW_CONTROLLER_PROPS_KEY);
        comment = getOptionalKeyAsType(map, COMMENT_KEY, String.class, FLOW_CONTROLLER_PROPS_KEY, DEFAULT_COMMENT);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> result = super.toMap();
        result.put(NAME_KEY, name);
        result.put(COMMENT_KEY, comment);
        return result;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }
}
