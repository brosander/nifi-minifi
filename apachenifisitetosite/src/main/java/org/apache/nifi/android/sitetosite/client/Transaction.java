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

import org.apache.nifi.android.sitetosite.client.protocol.CompressionOutputStream;
import org.apache.nifi.android.sitetosite.client.protocol.HttpMethod;
import org.apache.nifi.android.sitetosite.client.protocol.ResponseCode;
import org.apache.nifi.android.sitetosite.packet.DataPacket;
import org.apache.nifi.android.sitetosite.util.IOUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;

import static org.apache.nifi.android.sitetosite.client.SiteToSiteClient.HANDSHAKE_PROPERTY_BATCH_COUNT;
import static org.apache.nifi.android.sitetosite.client.SiteToSiteClient.HANDSHAKE_PROPERTY_BATCH_DURATION;
import static org.apache.nifi.android.sitetosite.client.SiteToSiteClient.HANDSHAKE_PROPERTY_BATCH_SIZE;
import static org.apache.nifi.android.sitetosite.client.SiteToSiteClient.HANDSHAKE_PROPERTY_REQUEST_EXPIRATION;
import static org.apache.nifi.android.sitetosite.client.SiteToSiteClient.HANDSHAKE_PROPERTY_USE_COMPRESSION;
import static org.apache.nifi.android.sitetosite.client.SiteToSiteClient.LOCATION_HEADER_NAME;
import static org.apache.nifi.android.sitetosite.client.SiteToSiteClient.LOCATION_URI_INTENT_NAME;
import static org.apache.nifi.android.sitetosite.client.SiteToSiteClient.LOCATION_URI_INTENT_VALUE;
import static org.apache.nifi.android.sitetosite.client.SiteToSiteClient.SERVER_SIDE_TRANSACTION_TTL;

public class Transaction {
    private static final Map<String, String> BEGIN_TRANSACTION_HEADERS = initBeginTransactionHeaders();
    private static final Map<String, String> END_TRANSACTION_HEADERS = initEndTransactionHeaders();

    private final Map<String, String> handshakeProperties;
    private final String transactionUrl;
    private final SiteToSiteClientRequestManager siteToSiteClientRequestManager;
    private final CRC32 crc;
    private final OutputStream outputStream;
    private final HttpURLConnection httpURLConnection;
    private final ScheduledFuture<?> ttlExtendFuture;

    public Transaction(String peerUrl, String authorization, String portIdentifier, SiteToSiteClientRequestManager siteToSiteClientRequestManager, SiteToSiteClientConfig siteToSiteClientConfig, ScheduledExecutorService ttlExtendTaskExecutor) throws IOException {
        this.siteToSiteClientRequestManager = siteToSiteClientRequestManager;
        this.handshakeProperties = createHandshakeProperties(siteToSiteClientConfig, authorization);

        HttpURLConnection createTransactionConnection = siteToSiteClientRequestManager.openConnection(peerUrl + "/data-transfer/input-ports/" + portIdentifier + "/transactions", handshakeProperties, HttpMethod.POST);

        int responseCode = createTransactionConnection.getResponseCode();
        if (responseCode < 200 || responseCode > 299) {
            throw new IOException("Got response code " + responseCode);
        }
        int ttl;
        if (LOCATION_URI_INTENT_VALUE.equals(createTransactionConnection.getHeaderField(LOCATION_URI_INTENT_NAME))) {
            String ttlString = createTransactionConnection.getHeaderField(SERVER_SIDE_TRANSACTION_TTL);
            if (ttlString == null || ttlString.isEmpty()) {
                throw new IOException("Expected " + SERVER_SIDE_TRANSACTION_TTL + " header");
            } else {
                try {
                    ttl = Integer.parseInt(ttlString);
                } catch (Exception e) {
                    throw new IOException("Unable to parse " + SERVER_SIDE_TRANSACTION_TTL + " as int: " + ttlString, e);
                }
            }
            transactionUrl = createTransactionConnection.getHeaderField(LOCATION_HEADER_NAME);
        } else {
            throw new IOException("Expected header " + LOCATION_URI_INTENT_NAME + " == " + LOCATION_URI_INTENT_VALUE);
        }

        crc = new CRC32();
        Map<String, String> beginTransactionHeaders = new HashMap<>(BEGIN_TRANSACTION_HEADERS);
        beginTransactionHeaders.putAll(handshakeProperties);
        this.httpURLConnection = siteToSiteClientRequestManager.openConnection(transactionUrl + "/flow-files", beginTransactionHeaders, HttpMethod.POST);
        OutputStream outputStream = this.httpURLConnection.getOutputStream();
        if (siteToSiteClientConfig.isUseCompression()) {
            outputStream = new CompressionOutputStream(outputStream);
        }
        outputStream = new CheckedOutputStream(outputStream, crc);
        this.outputStream = outputStream;
        ttlExtendFuture = ttlExtendTaskExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection ttlExtendConnection = Transaction.this.siteToSiteClientRequestManager.openConnection(transactionUrl, handshakeProperties, HttpMethod.PUT);
                    try {

                    } finally {
                        ttlExtendConnection.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, ttl / 2, ttl / 2, TimeUnit.SECONDS);
    }

    private Map<String, String> createHandshakeProperties(SiteToSiteClientConfig siteToSiteClientConfig, String authorization) {
        Map<String, String> handshakeProperties = new HashMap<>();

        if (siteToSiteClientConfig.isUseCompression()) {
            handshakeProperties.put(HANDSHAKE_PROPERTY_USE_COMPRESSION, "true");
        }

        long requestExpirationMillis = siteToSiteClientConfig.getIdleConnectionExpiration(TimeUnit.MILLISECONDS);
        if (requestExpirationMillis > 0) {
            handshakeProperties.put(HANDSHAKE_PROPERTY_REQUEST_EXPIRATION, String.valueOf(requestExpirationMillis));
        }

        int batchCount = siteToSiteClientConfig.getPreferredBatchCount();
        if (batchCount > 0) {
            handshakeProperties.put(HANDSHAKE_PROPERTY_BATCH_COUNT, String.valueOf(batchCount));
        }

        long batchSize = siteToSiteClientConfig.getPreferredBatchSize();
        if (batchSize > 0) {
            handshakeProperties.put(HANDSHAKE_PROPERTY_BATCH_SIZE, String.valueOf(batchSize));
        }

        long batchDurationMillis = siteToSiteClientConfig.getPreferredBatchDuration(TimeUnit.MILLISECONDS);
        if (batchDurationMillis > 0) {
            handshakeProperties.put(HANDSHAKE_PROPERTY_BATCH_DURATION, String.valueOf(batchDurationMillis));
        }

        if (authorization != null) {
            handshakeProperties.put("Authorization", authorization);
        }

        return Collections.unmodifiableMap(handshakeProperties);
    }

    private static Map<String, String> initEndTransactionHeaders() {
        Map<String, String> result = new HashMap<>();
        result.put("Content-Type", "application/octet-stream");
        return Collections.unmodifiableMap(result);
    }

    private static Map<String, String> initBeginTransactionHeaders() {
        Map<String, String> result = new HashMap<>();
        result.put("Content-Type", "application/octet-stream");
        result.put("Accept", "text/plain");
        return Collections.unmodifiableMap(result);
    }

    public void send(DataPacket dataPacket) throws IOException {
        final DataOutputStream out = new DataOutputStream(outputStream);

        final Map<String, String> attributes = dataPacket.getAttributes();
        out.writeInt(attributes.size());
        for (final Map.Entry<String, String> entry : attributes.entrySet()) {
            writeString(entry.getKey(), out);
            writeString(entry.getValue(), out);
        }

        out.writeLong(dataPacket.getSize());

        final InputStream in = dataPacket.getData();
        byte[] buf = new byte[1024];
        int read = 0;
        while ((read = in.read(buf)) != -1) {
            out.write(buf, 0, read);
        }
        out.flush();
    }

    private void writeString(final String val, final DataOutputStream out) throws IOException {
        final byte[] bytes = val.getBytes("UTF-8");
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    public void confirm() throws IOException {
        int responseCode = httpURLConnection.getResponseCode();
        if (responseCode != 200 && responseCode != 202) {
            throw new IOException("Got response code " + responseCode);
        }
        long calculatedCrc = crc.getValue();
        long serverCrc = IOUtils.readInputStreamAndParseAsLong(httpURLConnection.getInputStream());
        if (calculatedCrc != serverCrc) {
            endTransaction(ResponseCode.BAD_CHECKSUM);
            throw new IOException("Should have " + calculatedCrc + " for crc, got " + serverCrc);
        }
    }

    public void complete() throws IOException {
        endTransaction(ResponseCode.CONFIRM_TRANSACTION);
    }

    public void cancel() throws IOException {
        endTransaction(ResponseCode.CANCEL_TRANSACTION);
    }

    private void endTransaction(ResponseCode responseCodeToSend) throws IOException {
        ttlExtendFuture.cancel(false);
        try {
            ttlExtendFuture.get();
        } catch (Exception e) {
            if (!(e instanceof CancellationException)) {
                throw new IOException("Error waiting on ttl extension thread to end.", e);
            }
        }
        httpURLConnection.disconnect();
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("responseCode", Integer.toString(responseCodeToSend.getCode()));
        Map<String, String> endTransactionHeaders = new HashMap<>(END_TRANSACTION_HEADERS);
        endTransactionHeaders.putAll(handshakeProperties);
        HttpURLConnection delete = siteToSiteClientRequestManager.openConnection(transactionUrl, endTransactionHeaders, queryParameters, HttpMethod.DELETE);

        int responseCode = delete.getResponseCode();
        if (responseCode < 200 || responseCode > 299) {
            throw new IOException("Got response code " + responseCode);
        }

        delete.disconnect();
    }
}
