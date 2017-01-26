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

package org.apache.nifi.android.sitetosite.client;

import android.util.Base64;

import org.apache.nifi.android.sitetosite.client.protocol.HttpMethod;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import static org.apache.nifi.android.sitetosite.client.SiteToSiteClient.PROTOCOL_VERSION;

public class SiteToSiteClientRequestManager {
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    private final SSLSocketFactory socketFactory;
    private final Proxy proxy;
    private final String proxyAuth;

    public SiteToSiteClientRequestManager(SiteToSiteClientConfig siteToSiteClientConfig) {
        SSLContext sslContext = siteToSiteClientConfig.getSslContext();
        if (sslContext != null) {
            socketFactory = sslContext.getSocketFactory();
        } else {
            socketFactory = null;
        }
        proxy = getProxy(siteToSiteClientConfig);
        String proxyUsername = siteToSiteClientConfig.getProxyUsername();
        if (proxy != null && proxyUsername != null) {
            proxyAuth = Base64.encodeToString((proxyUsername + ":" + siteToSiteClientConfig.getProxyPassword()).getBytes(UTF_8), Base64.DEFAULT);
        } else {
            proxyAuth = null;
        }
    }

    public HttpURLConnection openConnection(String urlString) throws IOException {
        return openConnection(urlString, new HashMap<String, String>());
    }

    public HttpURLConnection openConnection(String urlString, Map<String, String> headers) throws IOException {
        return openConnection(urlString, headers, HttpMethod.GET);
    }

    public HttpURLConnection openConnection(String urlString, HttpMethod method) throws IOException {
        return openConnection(urlString, new HashMap<String, String>(), method);
    }

    public HttpURLConnection openConnection(String urlString, Map<String, String> headers, HttpMethod method) throws IOException {
        return openConnection(urlString, headers, new HashMap<String, String>(), method);
    }

    public HttpURLConnection openConnection(String urlString, Map<String, String> headers, Map<String, String> queryParameters, HttpMethod method) throws IOException {
        if (socketFactory == null) {
            if (!urlString.startsWith("http://")) {
                throw new IOException();
            }
        } else if (!urlString.startsWith("https://")) {
            throw new IOException();
        }

        String actualUrl = urlString;
        if (queryParameters.size() > 0) {
            actualUrl = urlString + "?" + urlEncodeParameters(queryParameters);
        }
        URL url = new URL(actualUrl);
        HttpURLConnection httpURLConnection;

        if (proxy == null) {
            httpURLConnection = (HttpURLConnection) url.openConnection();
        } else {
            httpURLConnection = (HttpURLConnection) url.openConnection(proxy);
        }

        if (socketFactory != null) {
            ((HttpsURLConnection)httpURLConnection).setSSLSocketFactory(socketFactory);
        }

        if (proxyAuth != null) {
            httpURLConnection.setRequestProperty("Proxy-Authorization", proxyAuth);
        }

        Map<String, String> finalHeaders = new HashMap<>(headers);

        if (!finalHeaders.containsKey("Accept")) {
            finalHeaders.put("Accept", "application/json");
        }

        if (!finalHeaders.containsKey(PROTOCOL_VERSION)) {
            finalHeaders.put(PROTOCOL_VERSION, "5");
        }

        for (Map.Entry<String, String> entry : finalHeaders.entrySet()) {
            httpURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
        }

        httpURLConnection.setRequestMethod(method.name());

        return httpURLConnection;
    }

    public static String urlEncodeParameters(Map<String, String> queryParameters) throws UnsupportedEncodingException {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : queryParameters.entrySet()) {
            stringBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            stringBuilder.append("=");
            stringBuilder.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            stringBuilder.append("&");
        }
        // Remove trailing &
        stringBuilder.setLength(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    protected Proxy getProxy(SiteToSiteClientConfig siteToSiteClientConfig) {
        String proxyHost = siteToSiteClientConfig.getProxyHost();
        if (proxyHost == null) {
            return null;
        }

        int proxyPort = siteToSiteClientConfig.getProxyPort();
        int port = 80;
        if (proxyPort <= 65535 && proxyPort > 0) {
            port = proxyPort;
        }
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, port));
    }
}
