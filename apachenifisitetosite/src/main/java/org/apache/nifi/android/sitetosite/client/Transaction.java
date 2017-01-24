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
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.GZIPOutputStream;

public class Transaction {
    private static final Map<String, String> BEGIN_TRANSACTION_HEADERS = initBeginTransactionHeaders();
    private static final Map<String, String> END_TRANSACTION_HEADERS = initEndTransactionHeaders();

    private final String transactionUrl;
    private final SiteToSiteClientRequestManager siteToSiteClientRequestManager;
    private final CRC32 crc;
    private final OutputStream outputStream;
    private final HttpURLConnection httpURLConnection;

    public Transaction(SiteToSiteClientRequestManager siteToSiteClientRequestManager, String transactionUrl, boolean compress) throws IOException {
        this.siteToSiteClientRequestManager = siteToSiteClientRequestManager;
        this.transactionUrl = transactionUrl;
        crc = new CRC32();
        httpURLConnection = siteToSiteClientRequestManager.openConnection(transactionUrl + "/flow-files", BEGIN_TRANSACTION_HEADERS, HttpMethod.POST);
        OutputStream outputStream = httpURLConnection.getOutputStream();
        if (compress) {
            outputStream = new GZIPOutputStream(outputStream);
        }
        outputStream = new CheckedOutputStream(outputStream, crc);
        this.outputStream = outputStream;
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
        httpURLConnection.disconnect();
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("responseCode", Integer.toString(responseCodeToSend.getCode()));
        HttpURLConnection delete = siteToSiteClientRequestManager.openConnection(transactionUrl, END_TRANSACTION_HEADERS, queryParameters, HttpMethod.DELETE);

        int responseCode = delete.getResponseCode();
        if (responseCode < 200 || responseCode > 299) {
            throw new IOException("Got response code " + responseCode);
        }

        delete.disconnect();
    }
}
