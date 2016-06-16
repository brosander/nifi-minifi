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

import org.yaml.snakeyaml.introspector.MethodProperty;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class DTORepresenter extends Representer {

    public static final String DTO = "dto";

    public DTORepresenter() {
        representers.put(null, data -> {
            if (data == null) {
                throw new RuntimeException();
            } else {
                Class<?> clazz = data.getClass();
                DTORepresent dtoRepresent = new DTORepresent(clazz, null);
                representers.put(clazz, dtoRepresent);
                return dtoRepresent.representData(data);
            }
        });
    }

    public static List<Property> getBooleanProperties(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> method.getReturnType() == Boolean.class && method.getName().startsWith("is"))
                .map(method -> {
                    try {
                        return new PropertyDescriptor(method.getName().substring(2), clazz, method.getName(), "set" + method.getName().substring(2));
                    } catch (IntrospectionException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(MethodProperty::new).collect(Collectors.toList());
    }

    public class DTORepresent implements Represent {
        private final Set<Property> properties;
        private final Object defaultObj;

        public DTORepresent(Class<?> clazz, Set<String> propertyNames) {
            Set<Property> properties;
            List<Property> booleanProperties = getBooleanProperties(clazz);
            if (propertyNames != null) {
                Map<String, Property> propertyMap;
                try {
                    propertyMap = getProperties(clazz).stream().sorted().collect(Collectors.toMap(Property::getName, property -> property));
                } catch (IntrospectionException e) {
                    throw new RuntimeException(e);
                }
                properties = propertyNames.stream().map(propertyMap::get).collect(Collectors.toCollection(TreeSet::new));
                booleanProperties = booleanProperties.stream().filter(booleanProperty -> propertyNames.contains(booleanProperty.getName())).collect(Collectors.toList());
            } else {
                try {
                    properties = getProperties(clazz);
                } catch (IntrospectionException e) {
                    throw new RuntimeException(e);
                }
            }
            properties.addAll(booleanProperties);
            this.properties = Collections.unmodifiableSet(properties);
            try {
                defaultObj = clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Node representData(Object data) {
            return representJavaBean(properties.stream().filter(property -> {
                Object defaultValue = property.get(defaultObj);
                Object currentValue = property.get(data);
                if (currentValue == null) {
                    return defaultValue != null;
                }
                return !currentValue.equals(defaultValue);
            }).collect(Collectors.toCollection(LinkedHashSet::new)), data);
        }
    }
}
