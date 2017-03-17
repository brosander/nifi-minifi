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
import com.palantir.docker.compose.connection.DockerPort;
import org.apache.commons.io.IOUtils;
import org.apache.nifi.minifi.commons.schema.ConfigSchema;
import org.apache.nifi.minifi.commons.schema.serialization.SchemaLoader;
import org.apache.nifi.security.util.KeyStoreUtils;
import org.apache.nifi.security.util.SslContextFactory;
import org.apache.nifi.toolkit.tls.commandLine.CommandLineParseException;
import org.apache.nifi.toolkit.tls.standalone.TlsToolkitStandalone;
import org.apache.nifi.toolkit.tls.standalone.TlsToolkitStandaloneCommandLine;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class FileSystemCacheProviderSecureTest {
    public static final String C2_URL = "https://c2:10443/c2/config";
    private static SSLSocketFactory healthCheckSocketFactory;

    // Not annotated as rule because we need to generate certificatesDirectory first
    public static DockerComposeRule docker = DockerComposeRule.builder()
            .file("target/test-classes/FileSystemCacheProviderSecureTest/docker-compose.yml")
            .waitingForServices(Arrays.asList("squid", "c2"),
                    new HttpsStatusCodeHealthCheck(container -> C2_URL, containers -> containers.get(0), containers -> containers.get(1), () -> healthCheckSocketFactory, 403))
            .build();
    private static Path certificatesDirectory;
    private static SSLContext trustSslContext;


    @BeforeClass
    public static void initCertificates() throws IOException, CommandLineParseException, GeneralSecurityException, InterruptedException {
        certificatesDirectory = Paths.get(FileSystemCacheProviderSecureTest.class.getClassLoader()
                .getResource("FileSystemCacheProviderSecureTest/docker-compose.yml").getFile()).getParent().toAbsolutePath().resolve("certificates");
        Files.createDirectories(certificatesDirectory);
        TlsToolkitStandaloneCommandLine tlsToolkitStandaloneCommandLine = new TlsToolkitStandaloneCommandLine();
        tlsToolkitStandaloneCommandLine.parse(new String[]{"-O", "-o", certificatesDirectory.toFile().getAbsolutePath(),
                "-n", "c2", "-C", "CN=user1", "-C", "CN=user2", "-C", "CN=user3", "-C", "CN=user4", "-S", "badKeyPass", "-K", "badKeyPass", "-P", "badTrustPass"});
        new TlsToolkitStandalone().createNifiKeystoresAndTrustStores(tlsToolkitStandaloneCommandLine.createConfig());

        tlsToolkitStandaloneCommandLine = new TlsToolkitStandaloneCommandLine();
        tlsToolkitStandaloneCommandLine.parse(new String[]{"-O", "-o", certificatesDirectory.getParent().resolve("badCert").toFile().getAbsolutePath(), "-C", "CN=user3"});
        new TlsToolkitStandalone().createNifiKeystoresAndTrustStores(tlsToolkitStandaloneCommandLine.createConfig());

        final KeyStore trustStore = KeyStoreUtils.getTrustStore("jks");
        try (final InputStream trustStoreStream = new FileInputStream(certificatesDirectory.resolve("c2").resolve("truststore.jks").toFile().getAbsolutePath())) {
            trustStore.load(trustStoreStream, "badTrustPass".toCharArray());
        }
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        trustSslContext = SslContextFactory.createTrustSslContext(certificatesDirectory.resolve("c2").resolve("truststore.jks").toFile().getAbsolutePath(), "badTrustPass".toCharArray(), "jks", "TLS");

        healthCheckSocketFactory = trustSslContext.getSocketFactory();
        docker.before();
    }

    @AfterClass
    public static void cleanup() {
        docker.after();
    }

    @Test
    public void testNoClientCert() throws Exception {
        assertReturnCode("", trustSslContext, 403);
        assertReturnCode("?class=raspi2", trustSslContext, 403);
        assertReturnCode("?class=raspi3", trustSslContext, 403);
    }

    @Test
    public void testUser1() throws Exception {
        SSLContext sslContext = loadSslContext("user1");

        assertReturnCode("", sslContext, 403);

        ConfigSchema configSchema = assertReturnCode("?class=raspi2", sslContext, 200);
        assertEquals("Raspi 2", configSchema.getFlowControllerProperties().getName());

        assertReturnCode("?class=raspi3", sslContext, 403);
    }

    @Test
    public void testUser2() throws Exception {
        SSLContext sslContext = loadSslContext("user2");

        assertReturnCode("", sslContext, 403);
        assertReturnCode("?class=raspi2", sslContext, 403);

        ConfigSchema configSchema = assertReturnCode("?class=raspi3", sslContext, 200);
        assertEquals("Raspi 3", configSchema.getFlowControllerProperties().getName());
    }

    @Test
    public void testUser3() throws Exception {
        SSLContext sslContext = loadSslContext("user3");

        assertReturnCode("", sslContext, 400);

        ConfigSchema configSchema = assertReturnCode("?class=raspi2", sslContext, 200);
        assertEquals("Raspi 2", configSchema.getFlowControllerProperties().getName());

        configSchema = assertReturnCode("?class=raspi3", sslContext, 200);
        assertEquals("Raspi 3", configSchema.getFlowControllerProperties().getName());
    }

    @Test(expected = IOException.class)
    public void testUser3WrongCA() throws Exception {
        assertReturnCode("?class=raspi3", loadSslContext("user3", certificatesDirectory.getParent().resolve("badCert")), 403);
    }

    @Test
    public void testUser4() throws Exception {
        SSLContext sslContext = loadSslContext("user4");

        assertReturnCode("", sslContext, 403);
        assertReturnCode("?class=raspi2", sslContext, 403);
        assertReturnCode("?class=raspi3", sslContext, 403);
    }

    private SSLContext loadSslContext(String username) throws GeneralSecurityException, IOException {
        return loadSslContext(username, certificatesDirectory);
    }

    private SSLContext loadSslContext(String username, Path directory) throws GeneralSecurityException, IOException {
        char[] keystorePasswd;
        try (InputStream inputStream = Files.newInputStream(directory.resolve("CN=" + username + ".password"))) {
            keystorePasswd = IOUtils.toString(inputStream, StandardCharsets.UTF_8).toCharArray();
        }
        return SslContextFactory.createSslContext(
                directory.resolve("CN=" + username + ".p12").toFile().getAbsolutePath(),
                keystorePasswd,
                "PKCS12",
                certificatesDirectory.resolve("c2").resolve("truststore.jks").toFile().getAbsolutePath(),
                "badTrustPass".toCharArray(), "jks", SslContextFactory.ClientAuth.NONE, "TLS");
    }

    private ConfigSchema assertReturnCode(String query, SSLContext sslContext, int expectedReturnCode) throws Exception {
        HttpsURLConnection httpsURLConnection = openUrlConnection(C2_URL + query, sslContext);
        try {
            assertEquals(expectedReturnCode, httpsURLConnection.getResponseCode());
            if (expectedReturnCode == 200) {
                return SchemaLoader.loadConfigSchemaFromYaml(httpsURLConnection.getInputStream());
            }
        } finally {
            httpsURLConnection.disconnect();
        }
        return null;
    }

    private HttpsURLConnection openUrlConnection(String url, SSLContext sslContext) throws IOException {
        DockerPort dockerPort = docker.containers().container("squid").port(3128);
        HttpsURLConnection httpURLConnection = (HttpsURLConnection) new URL(url).openConnection(
                new Proxy(Proxy.Type.HTTP, new InetSocketAddress(dockerPort.getIp(), dockerPort.getExternalPort())));
        httpURLConnection.setSSLSocketFactory(sslContext.getSocketFactory());
        return httpURLConnection;
    }
}
