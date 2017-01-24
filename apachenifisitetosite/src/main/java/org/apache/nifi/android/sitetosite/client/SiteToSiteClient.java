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

import android.util.JsonReader;

import org.apache.nifi.android.sitetosite.util.Charsets;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

@SuppressWarnings("deprecation")
public class SiteToSiteClient {
    public static final String LOCATION_HEADER_NAME = "Location";
    public static final String LOCATION_URI_INTENT_NAME = "x-location-uri-intent";
    public static final String LOCATION_URI_INTENT_VALUE = "transaction-url";

    public static final String PROTOCOL_VERSION = "x-nifi-site-to-site-protocol-version";
    public static final String SERVER_SIDE_TRANSACTION_TTL = "x-nifi-site-to-site-server-transaction-ttl";
    public static final String HANDSHAKE_PROPERTY_USE_COMPRESSION = "x-nifi-site-to-site-use-compression";
    public static final String HANDSHAKE_PROPERTY_REQUEST_EXPIRATION = "x-nifi-site-to-site-request-expiration";
    public static final String HANDSHAKE_PROPERTY_BATCH_COUNT = "x-nifi-site-to-site-batch-count";
    public static final String HANDSHAKE_PROPERTY_BATCH_SIZE = "x-nifi-site-to-site-batch-size";
    public static final String HANDSHAKE_PROPERTY_BATCH_DURATION = "x-nifi-site-to-site-batch-duration";
    public static final String CANONICAL_NAME = SiteToSiteClient.class.getCanonicalName();

    private final PeerTracker peerTracker;
    private final String portIdentifier;
    private final SiteToSiteClientRequestManager siteToSiteClientRequestManager;

    public SiteToSiteClient(SiteToSiteClientConfig siteToSiteClientConfig) throws IOException {
        siteToSiteClientRequestManager = new SiteToSiteClientRequestManager(siteToSiteClientConfig);
        peerTracker = new PeerTracker(siteToSiteClientRequestManager, siteToSiteClientConfig.getUrls());
        String portIdentifier = siteToSiteClientConfig.getPortIdentifier();
        if (portIdentifier == null) {
            this.portIdentifier = getPortIdentifier(siteToSiteClientConfig.getPortName());
        } else {
            this.portIdentifier = portIdentifier;
        }
    }

    private String getPortIdentifier(String portName) throws IOException {
        HttpURLConnection httpURLConnection = peerTracker.execute("/site-to-site");
        try {
            return getPortIdentifier(httpURLConnection.getInputStream(), portName);
        } finally {
            httpURLConnection.disconnect();
        }
    }

    private String getPortIdentifier(InputStream inputStream, String portName) throws IOException {
        JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream, Charsets.UTF_8));
        try {
            return getPortIdentifierFromController(portName, jsonReader);
        } finally {
            jsonReader.close();
        }
    }

    private String getPortIdentifierFromController(String portName, JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        String id = null;
        while (jsonReader.hasNext()) {
            if (id == null && "controller".equals(jsonReader.nextName())) {
                id = getPortIdentifierFromInputPorts(portName, jsonReader);
            } else {
                jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        return id;
    }

    private String getPortIdentifierFromInputPorts(String portName, JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        String id = null;
        while (jsonReader.hasNext()) {
            if ("inputPorts".equals(jsonReader.nextName())) {
                jsonReader.beginArray();
                while (jsonReader.hasNext()) {
                    if (id == null) {
                        id = getPortIdentifierFromInputPort(portName, jsonReader);
                    } else {
                        jsonReader.skipValue();
                    }
                }
                jsonReader.endArray();
            } else {
                jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        return id;
    }

    private String getPortIdentifierFromInputPort(String portName, JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        String id = null;
        String name = null;
        while (jsonReader.hasNext()) {
            String key = jsonReader.nextName();
            if (id == null && "id".equals(key)) {
                id = jsonReader.nextString();
            } else if (name == null && "name".equals(key)) {
                name = jsonReader.nextString();
            } else {
                jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        if (portName.equals(name)) {
            return id;
        }
        return null;
    }

    public Transaction createTransaction() throws IOException {
        HttpURLConnection httpURLConnection = peerTracker.execute("/data-transfer/input-ports/" + portIdentifier + "/transactions");
        try {
            httpURLConnection.setRequestMethod("POST");

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode < 200 || responseCode > 299) {
                throw new IOException("Got response code " + responseCode);
            }
            if (LOCATION_URI_INTENT_VALUE.equals(httpURLConnection.getHeaderField(LOCATION_URI_INTENT_NAME))) {
                return new Transaction(siteToSiteClientRequestManager, httpURLConnection.getHeaderField(LOCATION_HEADER_NAME), false);
            }
        } finally {
            httpURLConnection.disconnect();
        }
        return null;
    }
}
