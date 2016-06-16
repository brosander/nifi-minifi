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

package org.apache.nifi.minifi.bootstrap.util.yaml;

import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeId;

import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DTOConstructor extends Constructor {
    public DTOConstructor() {
        yamlClassConstructors.put(NodeId.mapping, new DTOConstruct());
    }

    public class DTOConstruct extends ConstructMapping {
        private Map<Class<?>, Map<String, Property>> booleanProps = new HashMap<>();

        @Override
        protected Property getProperty(Class<? extends Object> type, String name) throws IntrospectionException {
            try {
                return super.getProperty(type, name);
            } catch (YAMLException e) {
                Map<String, Property> namePropertyMap = booleanProps.get(type);
                if (namePropertyMap == null) {
                    namePropertyMap = new HashMap<>();
                    booleanProps.put(type, namePropertyMap);
                }
                Property property = namePropertyMap.get(name);
                if (property == null) {
                    if (namePropertyMap.containsKey(name)) {
                        throw e;
                    } else {
                        Optional<Property> first = DTORepresenter.getBooleanProperties(type).stream()
                                .filter(booleanProperty -> booleanProperty.getName().equals(name))
                                .findFirst();
                        namePropertyMap.put(name, first.orElse(null));
                        return first.orElseThrow(() -> e);
                    }
                } else {
                    return property;
                }
            }
        }
    }
}