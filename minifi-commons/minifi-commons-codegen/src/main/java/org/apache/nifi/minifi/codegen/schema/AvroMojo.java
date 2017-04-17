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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.velocity.VelocityContext;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mojo(name = "avro", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class AvroMojo extends BaseCodegenMojo {
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/avro", property = "outputDir", required = true)
    private File outputDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        outputDir.mkdirs();
        for (File file : inputDir.listFiles()) {
            String outName = file.getName();
            int firstPeriod = outName.indexOf('.');
            if (firstPeriod >= 0) {
                outName = outName.substring(0, firstPeriod) + ".avsc";
            }
            SchemaDefinition schemaDefinition = getSchemaDefinition(file);
            try {
                getAllEnums(schemaDefinition);
            } catch (Exception e) {
                throw new MojoExecutionException("Unable to get enums.", e);
            }
            Map<String, Integer> referenceMap = getReferenceMap(schemaDefinition);
            schemaDefinition.setClassComparator(new Comparator<ClassDefinition>() {
                @Override
                public int compare(ClassDefinition o1, ClassDefinition o2) {
                    int indexCompare = referenceMap.get(o1.getName()) - referenceMap.get(o2.getName());
                    if (indexCompare != 0) {
                        return indexCompare;
                    }
                    return o1.getName().compareTo(o2.getName());
                }
            });
            VelocityContext context = new VelocityContext();
            context.put("schema", schemaDefinition);
            context.put("avro", new AvroUtil());
            context.put("util", new Util());
            render("avro.vm", context, new File(outputDir, outName));
        }
    }

    private void getAllEnums(SchemaDefinition schemaDefinition) throws Exception {
        Map<String, EnumDefinition> enums = schemaDefinition.getEnumMap();
        for (ClassDefinition classDefinition : schemaDefinition.getClasses()) {
            for (FieldDefinition fieldDefinition : classDefinition.getFields()) {
                addTypeEnum(enums, schemaDefinition, classDefinition, fieldDefinition.getType());
            }
        }
        List<EnumDefinition> enumDefinitions = new ArrayList<>(enums.values());
        enumDefinitions.sort(new Comparator<EnumDefinition>() {
            @Override
            public int compare(EnumDefinition o1, EnumDefinition o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        schemaDefinition.setEnums(enumDefinitions);
    }

    private void addTypeEnum(Map<String, EnumDefinition> enumDefinitions, SchemaDefinition schemaDefinition, ClassDefinition classDefinition, TypeDefinition typeDefinition)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (typeDefinition.isEnum()) {
            if (!enumDefinitions.containsKey(typeDefinition.getName())) {
                EnumDefinition enumDefinition = new EnumDefinition();
                enumDefinition.setParent(schemaDefinition);
                CanonicalName canonicalName = classDefinition.getCanonicalName(typeDefinition.getName());
                enumDefinition.setPackage(canonicalName.getPackage());
                enumDefinition.setName(canonicalName.getName());
                List<String> values = new ArrayList<>();
                for (Object value : Arrays.asList((Object[]) Class.forName(canonicalName.getCanonicalName()).getMethod("values").invoke(null))) {
                    values.add(value.toString());
                }
                enumDefinition.setValues(values);
                enumDefinitions.put(typeDefinition.getName(), enumDefinition);
            }
        } else {
            for (TypeDefinition definition : typeDefinition.getGenericTypes()) {
                addTypeEnum(enumDefinitions, schemaDefinition, classDefinition, definition);
            }
        }
    }

    private Map<String, Integer> getReferenceMap(SchemaDefinition schemaDefinition) {
        Map<String, Set<String>> dependencyMap = new HashMap<>();
        for (ClassDefinition classDefinition : schemaDefinition.getClasses()) {
            dependencyMap.put(classDefinition.getName(), new HashSet<>());
        }
        for (ClassDefinition classDefinition : schemaDefinition.getClasses()) {
            for (FieldDefinition fieldDefinition : classDefinition.getFields()) {
                addType(dependencyMap, classDefinition.getName(), fieldDefinition.getType());
            }
        }
        Set<String> resolved = new HashSet<>();
        Set<String> unresolved = new HashSet<>();
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, Set<String>> stringSetEntry : dependencyMap.entrySet()) {
            if (stringSetEntry.getValue().size() == 0) {
                resolved.add(stringSetEntry.getKey());
                result.put(stringSetEntry.getKey(), 0);
            } else {
                unresolved.add(stringSetEntry.getKey());
            }
        }
        int index = 1;
        while (unresolved.size() > 0) {
            Set<String> newlyResolved = new HashSet<>();
            for (String unresolvedClassname : unresolved) {
                if (resolved.containsAll(dependencyMap.get(unresolvedClassname))) {
                    newlyResolved.add(unresolvedClassname);
                    result.put(unresolvedClassname, index);
                }
            }
            unresolved.removeAll(newlyResolved);
            resolved.addAll(newlyResolved);
            index++;
        }
        return result;
    }

    private void addType(Map<String, Set<String>> map, String className, TypeDefinition typeDefinition) {
        if (!className.equals(typeDefinition.getName()) && map.containsKey(typeDefinition.getName())) {
            map.get(className).add(typeDefinition.getName());
        }
        for (TypeDefinition definition : typeDefinition.getGenericTypes()) {
            addType(map, className, definition);
        }
    }
}
