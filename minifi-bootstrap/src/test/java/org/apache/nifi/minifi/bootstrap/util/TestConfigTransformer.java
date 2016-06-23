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

package org.apache.nifi.minifi.bootstrap.util;

import org.apache.nifi.minifi.bootstrap.exception.InvalidConfigurationException;
import org.apache.nifi.util.StringUtils;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestConfigTransformer {

    @Test
    public void doesTransformFile() throws Exception {
        ConfigTransformer.transformConfigFile("./src/test/resources/config.yml", "./target/");
        File nifiPropertiesFile = new File("./target/nifi.properties");

        assertTrue(nifiPropertiesFile.exists());
        assertTrue(nifiPropertiesFile.canRead());

        nifiPropertiesFile.deleteOnExit();

        File flowXml = new File("./target/flow.xml.gz");
        assertTrue(flowXml.exists());
        assertTrue(flowXml.canRead());

        flowXml.deleteOnExit();
    }

    @Test
    public void doesTransformInputStream() throws Exception {
        File inputFile = new File("./src/test/resources/config.yml");
        ConfigTransformer.transformConfigFile(new FileInputStream(inputFile), "./target/");

        File nifiPropertiesFile = new File("./target/nifi.properties");
        assertTrue(nifiPropertiesFile.exists());
        assertTrue(nifiPropertiesFile.canRead());

        nifiPropertiesFile.deleteOnExit();

        File flowXml = new File("./target/flow.xml.gz");
        assertTrue(flowXml.exists());
        assertTrue(flowXml.canRead());

        flowXml.deleteOnExit();
    }

    @Test
    public void doesTransformOnDefaultFile() throws Exception {
        ConfigTransformer.transformConfigFile("./src/test/resources/default.yml", "./target/");
        File nifiPropertiesFile = new File("./target/nifi.properties");

        assertTrue(nifiPropertiesFile.exists());
        assertTrue(nifiPropertiesFile.canRead());

        nifiPropertiesFile.deleteOnExit();

        File flowXml = new File("./target/flow.xml.gz");
        assertTrue(flowXml.exists());
        assertTrue(flowXml.canRead());

        flowXml.deleteOnExit();
    }

    @Test
    public void doesTransformOnMultipleProcessors() throws Exception {
        ConfigTransformer.transformConfigFile("./src/test/resources/config-multiple-processors.yml", "./target/");
        File nifiPropertiesFile = new File("./target/nifi.properties");

        assertTrue(nifiPropertiesFile.exists());
        assertTrue(nifiPropertiesFile.canRead());

        nifiPropertiesFile.deleteOnExit();

        File flowXml = new File("./target/flow.xml.gz");
        assertTrue(flowXml.exists());
        assertTrue(flowXml.canRead());

        flowXml.deleteOnExit();
    }

    @Test
    public void doesTransformOnMultipleRemoteProcessingGroups() throws Exception {
        ConfigTransformer.transformConfigFile("./src/test/resources/config-multiple-RPGs.yml", "./target/");
        File nifiPropertiesFile = new File("./target/nifi.properties");

        assertTrue(nifiPropertiesFile.exists());
        assertTrue(nifiPropertiesFile.canRead());

        nifiPropertiesFile.deleteOnExit();

        File flowXml = new File("./target/flow.xml.gz");
        assertTrue(flowXml.exists());
        assertTrue(flowXml.canRead());

        flowXml.deleteOnExit();
    }

    @Test
    public void doesTransformOnMultipleInputPorts() throws Exception {
        ConfigTransformer.transformConfigFile("./src/test/resources/config-multiple-input-ports.yml", "./target/");
        File nifiPropertiesFile = new File("./target/nifi.properties");

        assertTrue(nifiPropertiesFile.exists());
        assertTrue(nifiPropertiesFile.canRead());

        nifiPropertiesFile.deleteOnExit();

        File flowXml = new File("./target/flow.xml.gz");
        assertTrue(flowXml.exists());
        assertTrue(flowXml.canRead());

        flowXml.deleteOnExit();
    }

    @Test
    public void doesTransformOnMinimal() throws Exception {
        ConfigTransformer.transformConfigFile("./src/test/resources/config-minimal.yml", "./target/");
        File nifiPropertiesFile = new File("./target/nifi.properties");

        assertTrue(nifiPropertiesFile.exists());
        assertTrue(nifiPropertiesFile.canRead());

        nifiPropertiesFile.deleteOnExit();

        File flowXml = new File("./target/flow.xml.gz");
        assertTrue(flowXml.exists());
        assertTrue(flowXml.canRead());

        flowXml.deleteOnExit();
    }

    @Test
    public void handleTransformInvalidFile() throws Exception {
        try {
            ConfigTransformer.transformConfigFile("./src/test/resources/config-invalid.yml", "./target/");
            fail("Invalid configuration file was not detected.");
        } catch (InvalidConfigurationException e){
            assertEquals("Provided YAML configuration is not a Map", e.getMessage());
        }
    }

    @Test
    public void handleTransformMalformedField() throws Exception {
        try {
            ConfigTransformer.transformConfigFile("./src/test/resources/config-malformed-field.yml", "./target/");
            fail("Invalid configuration file was not detected.");
        } catch (InvalidConfigurationException e){
            assertEquals("Failed to transform config file due to:['threshold' in section 'Swap' because it is found but could not be parsed as a Number]", e.getMessage());
        }
    }

    @Test
    public void handleTransformEmptyFile() throws Exception {
        try {
            ConfigTransformer.transformConfigFile("./src/test/resources/config-empty.yml", "./target/");
            fail("Invalid configuration file was not detected.");
        } catch (InvalidConfigurationException e){
            assertEquals("Provided YAML configuration is not a Map", e.getMessage());
        }
    }

    @Test
    public void handleTransformFileMissingRequiredField() throws Exception {
        try {
            ConfigTransformer.transformConfigFile("./src/test/resources/config-missing-required-field.yml", "./target/");
            fail("Invalid configuration file was not detected.");
        } catch (InvalidConfigurationException e){
            assertEquals("Failed to transform config file due to:['class' in section 'Processors' because it was not found and it is required]", e.getMessage());
        }
    }

    @Test
    public void handleTransformFileMultipleProblems() throws Exception {
        try {
            ConfigTransformer.transformConfigFile("./src/test/resources/config-multiple-problems.yml", "./target/");
            fail("Invalid configuration file was not detected.");
        } catch (InvalidConfigurationException e){
            assertEquals("Failed to transform config file due to:['scheduling strategy' in section 'Provenance Reporting' because it is not a valid scheduling strategy], ['class' in section " +
                    "'Processors' because it was not found and it is required], ['source name' in section 'Connections' because it was not found and it is required]", e.getMessage());
        }
    }

    @Test
    public void testTransformRoundTrip() throws IOException, JAXBException, InvalidConfigurationException {
        Map<String, Object> templateMap = ConfigTransformer.transformTemplateToMap(getClass().getClassLoader().getResourceAsStream("Working_with_Logs.xml"));
        Map<String, Object> yamlMap = ConfigTransformer.loadYamlAsMap(getClass().getClassLoader().getResourceAsStream("Working_with_Logs.yml"));
        assertNoMapDifferences(templateMap, yamlMap);
    }

    private void assertNoMapDifferences(Map<String, Object> templateMap, Map<String, Object> yamlMap) {
        List<String> differences = new ArrayList<>();
        getMapDifferences("", differences, templateMap, yamlMap);
        if (differences.size() > 0) {
            fail(String.join("\n", differences.toArray(new String[differences.size()])));
        }
    }

    private void getMapDifferences(String path, List<String> differences, Map<String, Object> expected, Map<String, Object> actual) {
        for (Map.Entry<String, Object> stringObjectEntry : expected.entrySet()) {
            String key = stringObjectEntry.getKey();
            String newPath = StringUtils.isEmpty(path) ? key : path + "." + key;
            if (!actual.containsKey(key)) {
                differences.add("Missing key: " + newPath);
            } else {
                getObjectDifferences(newPath, differences, stringObjectEntry.getValue(), actual.get(key));
            }
        }

        Set<String> extraKeys = new HashSet<>(actual.keySet());
        extraKeys.removeAll(expected.keySet());
        for (String extraKey : extraKeys) {
            differences.add("Extra key: " + path + extraKey);
        }
    }

    private void getListDifferences(String path, List<String> differences, List<Object> expected, List<Object> actual) {
        if (expected.size() == actual.size()) {
            for (int i = 0; i < expected.size(); i++) {
                getObjectDifferences(path + "[" + i + "]", differences, expected.get(i), actual.get(i));
            }
        } else {
            differences.add("Expect size of " + expected.size() + " for list at " + path + " but got " + actual.size());
        }
    }

    private void getObjectDifferences(String path, List<String> differences, Object expectedValue, Object actualValue) {
        if (expectedValue instanceof Map) {
            if (actualValue instanceof Map) {
                getMapDifferences(path, differences, (Map) expectedValue, (Map) actualValue);
            } else {
                differences.add("Expected map at " + path + " but got " + actualValue);
            }
        } else if (expectedValue instanceof List) {
            if (actualValue instanceof List) {
                getListDifferences(path, differences, (List) expectedValue, (List) actualValue);
            } else {
                differences.add("Expected map at " + path + " but got " + actualValue);
            }
        } else if (expectedValue == null) {
            if (actualValue != null) {
                differences.add("Expected null at " + path + " but got " + actualValue);
            }
        } else if (expectedValue instanceof Number) {
            if (actualValue instanceof Number) {
                if (!expectedValue.toString().equals(actualValue.toString())) {
                    differences.add("Expected value of " + expectedValue + " at " + path + " but got " + actualValue);
                }
            } else {
                differences.add("Expected Number at " + path + " but got " + actualValue);
            }
        } else if (!expectedValue.equals(actualValue)) {
            differences.add("Expected " + expectedValue + " at " + path + " but got " + actualValue);
        }
    }
}
