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

package org.apache.nifi.minifi.c2.provider.util;

import org.apache.nifi.minifi.c2.api.ConfigurationProviderException;
import org.apache.nifi.minifi.c2.api.InvalidParameterException;
import org.apache.nifi.minifi.c2.api.properties.C2Properties;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpConnector {
    private static final Logger logger = LoggerFactory.getLogger(HttpConnector.class);

    private final String baseUrl;
    private final SslContextFactory sslContextFactory;

    public HttpConnector(String baseUrl) throws InvalidParameterException, GeneralSecurityException, IOException {
        if (baseUrl.startsWith("https:")) {
            sslContextFactory = C2Properties.getInstance().getSslContextFactory();
            if (sslContextFactory == null) {
                throw new InvalidParameterException("Need sslContextFactory to connect to https endpoint (" + baseUrl + ")");
            }
        } else {
            sslContextFactory = null;
        }
        this.baseUrl = baseUrl;
    }

    public HttpURLConnection get(String endpointPath) throws ConfigurationProviderException {
        return get(endpointPath, Collections.emptyMap());
    }

    public HttpURLConnection get(String endpointPath, Map<String, List<String>> headers) throws ConfigurationProviderException {
        String endpointUrl = baseUrl + endpointPath;
        if (logger.isDebugEnabled()) {
            logger.debug("Connecting to endpoint: " + endpointUrl);
        }
        URL url;
        try {
            url = new URL(endpointUrl);
        } catch (MalformedURLException e) {
            throw new ConfigurationProviderException("Malformed url " + endpointUrl, e);
        }

        HttpURLConnection httpURLConnection;
        try {
            if (sslContextFactory == null) {
                httpURLConnection = (HttpURLConnection) url.openConnection();
            } else {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                SSLContext sslContext = sslContextFactory.getSslContext();
                SSLSocketFactory socketFactory = sslContext.getSocketFactory();
                httpsURLConnection.setSSLSocketFactory(socketFactory);
                httpURLConnection = httpsURLConnection;
            }
        } catch (IOException e) {
            throw new ConfigurationProviderException("Unable to connect to " + url, e);
        }
        headers.forEach((s, strings) -> httpURLConnection.setRequestProperty(s, strings.stream().collect(Collectors.joining(","))));
        return httpURLConnection;
    }
}
