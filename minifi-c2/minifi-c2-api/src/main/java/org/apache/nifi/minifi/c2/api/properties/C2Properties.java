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

package org.apache.nifi.minifi.c2.api.properties;

import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Properties;

public class C2Properties extends Properties {
    private static String C2_SERVER_HOME = System.getenv("C2_SERVER_HOME");

    private static final C2Properties properties = initProperties();

    private static C2Properties initProperties() {
        C2Properties properties = new C2Properties();
        try (InputStream inputStream = C2Properties.class.getClassLoader().getResourceAsStream("c2.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load c2.properties", e);
        }
        return properties;
    }

    public static C2Properties getInstance() {
        return properties;
    }

    public SslContextFactory getSslContextFactory() throws GeneralSecurityException, IOException {
        if (!Boolean.valueOf(getProperty("minifi.c2.server.secure", "false"))) {
            return null;
        }

        SslContextFactory sslContextFactory = new SslContextFactory();
        KeyStore keyStore = KeyStore.getInstance(properties.getProperty("minifi.c2.server.keystoreType"));
        try (InputStream inputStream = Files.newInputStream(Paths.get(C2_SERVER_HOME).resolve(properties.getProperty("minifi.c2.server.keystore")))) {
            keyStore.load(inputStream, properties.getProperty("minifi.c2.server.keystorePasswd").toCharArray());
        }
        sslContextFactory.setKeyStore(keyStore);
        sslContextFactory.setKeyManagerPassword(properties.getProperty("minifi.c2.server.keystorePasswd"));
        sslContextFactory.setWantClientAuth(true);

        sslContextFactory.setTrustStorePath(properties.getProperty("minifi.c2.server.truststore"));
        sslContextFactory.setTrustStoreType(properties.getProperty("minifi.c2.server.truststoreType"));
        sslContextFactory.setTrustStorePassword(properties.getProperty("minifi.c2.server.truststorePasswd"));
        try {
            sslContextFactory.start();
        } catch (Exception e) {
            throw new IOException(e);
        }
        return sslContextFactory;
    }
}
