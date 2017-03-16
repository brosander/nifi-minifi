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

package org.apache.nifi.minifi.c2.integration.test;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.Container;
import com.palantir.docker.compose.connection.DockerPort;
import org.apache.nifi.minifi.commons.schema.ConfigSchema;
import org.apache.nifi.minifi.commons.schema.exception.SchemaLoaderException;
import org.apache.nifi.minifi.commons.schema.serialization.SchemaLoader;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class FileSystemCacheProviderUnsecureTest {
    private String c2Url;

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
            .file("target/test-classes/FileSystemCacheProviderUnsecureTest/docker-compose.yml")
            .waitingForService("c2", new HttpStatusCodeHealthCheck(FileSystemCacheProviderUnsecureTest::getConfigUrl, 400))
            .build();

    public static String getConfigUrl(Container container) {
        DockerPort dockerPort = container.port(10080);
        return "http://" + dockerPort.getIp() + ":" + dockerPort.getExternalPort() + "/c2/config";
    }

    @Before
    public void setup() {
        c2Url = getConfigUrl(docker.containers().container("c2"));
    }

    @Test
    public void testCurrentVersion() throws IOException, SchemaLoaderException {
        ConfigSchema configSchema = getConfigSchema(c2Url + "?class=raspi3");
        assertEquals(3, configSchema.getVersion());
        assertEquals("MiNiFi Flow V2", configSchema.getFlowControllerProperties().getName());
    }

    @Test
    public void testVersion1() throws IOException, SchemaLoaderException {
        ConfigSchema configSchema = getConfigSchema(c2Url + "?class=raspi3&version=1");
        assertEquals(3, configSchema.getVersion());
        assertEquals("MiNiFi Flow", configSchema.getFlowControllerProperties().getName());
    }

    @Test
    public void testVersion2() throws IOException, SchemaLoaderException {
        ConfigSchema configSchema = getConfigSchema(c2Url + "?class=raspi3&version=2");
        assertEquals(3, configSchema.getVersion());
        assertEquals("MiNiFi Flow V2", configSchema.getFlowControllerProperties().getName());
    }

    @Test
    public void testUnacceptable() throws IOException {
        URL url = new URL(c2Url + "?class=raspi3");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            urlConnection.setRequestProperty("Accept", "text/xml");
            assertEquals(406, urlConnection.getResponseCode());
        } finally {
            urlConnection.disconnect();
        }
    }

    @Test
    public void testInvalid() throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) new URL(c2Url).openConnection();
        try {
            assertEquals(400, urlConnection.getResponseCode());
        } finally {
            urlConnection.disconnect();
        }
    }

    public ConfigSchema getConfigSchema(String urlString) throws IOException, SchemaLoaderException {
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        ConfigSchema configSchema;
        try (InputStream inputStream = urlConnection.getInputStream()) {
            configSchema = SchemaLoader.loadConfigSchemaFromYaml(inputStream);
        } finally {
            urlConnection.disconnect();
        }
        return configSchema;
    }
}
