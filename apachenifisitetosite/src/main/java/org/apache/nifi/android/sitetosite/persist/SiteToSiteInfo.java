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

package org.apache.nifi.android.sitetosite.persist;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.apache.nifi.remote.client.KeystoreType;
import org.apache.nifi.remote.client.SiteToSiteClient;
import org.apache.nifi.remote.protocol.SiteToSiteTransportProtocol;
import org.apache.nifi.remote.protocol.http.HttpProxy;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SiteToSiteInfo {
    private static final String SHARED_PREF_KEY = SiteToSiteInfo.class.getCanonicalName() + ".SHARED_PREF_KEY";
    private Set<String> urls;
    private long timeoutNanos = TimeUnit.SECONDS.toNanos(30);
    private long penalizationNanos = TimeUnit.SECONDS.toNanos(3);
    private long idleExpirationNanos = TimeUnit.SECONDS.toNanos(30);
    private String keystoreFilename;
    private String keystorePass;
    private KeystoreType keystoreType;
    private String truststoreFilename;
    private String truststorePass;
    private KeystoreType truststoreType;
    private boolean useCompression;
    private SiteToSiteTransportProtocol transportProtocol;
    private String portName;
    private String portIdentifier;
    private int batchCount;
    private long batchSize;
    private long batchNanos;
    private String proxyHost;
    private Integer proxyPort;
    private String proxyUsername;
    private String proxyPassword;

    public SiteToSiteInfo() {
    }

    public SiteToSiteInfo(Set<String> urls, long timeoutNanos, long penalizationNanos, long idleExpirationNanos, String keystoreFilename, String keystorePass, KeystoreType keystoreType, String truststoreFilename, String truststorePass, KeystoreType truststoreType, boolean useCompression, SiteToSiteTransportProtocol transportProtocol, String portName, String portIdentifier, int batchCount, long batchSize, long batchNanos, String proxyHost, Integer proxyPort, String proxyUsername, String proxyPassword) {
        this.urls = urls;
        this.timeoutNanos = timeoutNanos;
        this.penalizationNanos = penalizationNanos;
        this.idleExpirationNanos = idleExpirationNanos;
        this.keystoreFilename = keystoreFilename;
        this.keystorePass = keystorePass;
        this.keystoreType = keystoreType;
        this.truststoreFilename = truststoreFilename;
        this.truststorePass = truststorePass;
        this.truststoreType = truststoreType;
        this.useCompression = useCompression;
        this.transportProtocol = transportProtocol;
        this.portName = portName;
        this.portIdentifier = portIdentifier;
        this.batchCount = batchCount;
        this.batchSize = batchSize;
        this.batchNanos = batchNanos;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
    }

    public Set<String> getUrls() {
        return urls;
    }

    public void setUrls(Set<String> urls) {
        this.urls = urls;
    }

    public long getTimeoutNanos() {
        return timeoutNanos;
    }

    public void setTimeoutNanos(long timeoutNanos) {
        this.timeoutNanos = timeoutNanos;
    }

    public long getPenalizationNanos() {
        return penalizationNanos;
    }

    public void setPenalizationNanos(long penalizationNanos) {
        this.penalizationNanos = penalizationNanos;
    }

    public long getIdleExpirationNanos() {
        return idleExpirationNanos;
    }

    public void setIdleExpirationNanos(long idleExpirationNanos) {
        this.idleExpirationNanos = idleExpirationNanos;
    }

    public String getKeystoreFilename() {
        return keystoreFilename;
    }

    public void setKeystoreFilename(String keystoreFilename) {
        this.keystoreFilename = keystoreFilename;
    }

    public String getKeystorePass() {
        return keystorePass;
    }

    public void setKeystorePass(String keystorePass) {
        this.keystorePass = keystorePass;
    }

    public KeystoreType getKeystoreType() {
        return keystoreType;
    }

    public void setKeystoreType(KeystoreType keystoreType) {
        this.keystoreType = keystoreType;
    }

    public String getTruststoreFilename() {
        return truststoreFilename;
    }

    public void setTruststoreFilename(String truststoreFilename) {
        this.truststoreFilename = truststoreFilename;
    }

    public String getTruststorePass() {
        return truststorePass;
    }

    public void setTruststorePass(String truststorePass) {
        this.truststorePass = truststorePass;
    }

    public KeystoreType getTruststoreType() {
        return truststoreType;
    }

    public void setTruststoreType(KeystoreType truststoreType) {
        this.truststoreType = truststoreType;
    }

    public boolean isUseCompression() {
        return useCompression;
    }

    public void setUseCompression(boolean useCompression) {
        this.useCompression = useCompression;
    }

    public SiteToSiteTransportProtocol getTransportProtocol() {
        return transportProtocol;
    }

    public void setTransportProtocol(SiteToSiteTransportProtocol transportProtocol) {
        this.transportProtocol = transportProtocol;
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public String getPortIdentifier() {
        return portIdentifier;
    }

    public void setPortIdentifier(String portIdentifier) {
        this.portIdentifier = portIdentifier;
    }

    public int getBatchCount() {
        return batchCount;
    }

    public void setBatchCount(int batchCount) {
        this.batchCount = batchCount;
    }

    public long getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(long batchSize) {
        this.batchSize = batchSize;
    }

    public long getBatchNanos() {
        return batchNanos;
    }

    public void setBatchNanos(long batchNanos) {
        this.batchNanos = batchNanos;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public SiteToSiteClient createSiteToSiteClient() {
        SiteToSiteClient.Builder builder = new SiteToSiteClient.Builder()
                .urls(urls)
                .timeout(timeoutNanos, TimeUnit.NANOSECONDS)
                .nodePenalizationPeriod(penalizationNanos, TimeUnit.NANOSECONDS)
                .idleExpiration(idleExpirationNanos, TimeUnit.NANOSECONDS)
                .keystoreFilename(keystoreFilename)
                .keystorePass(keystorePass)
                .keystoreType(keystoreType)
                .truststoreFilename(truststoreFilename)
                .truststorePass(truststorePass)
                .truststoreType(truststoreType)
                .useCompression(useCompression)
                .transportProtocol(transportProtocol)
                .portName(portName)
                .portIdentifier(portIdentifier)
                .requestBatchCount(batchCount)
                .requestBatchSize(batchSize)
                .requestBatchDuration(batchNanos, TimeUnit.NANOSECONDS);
        if (proxyHost != null) {
            builder = builder.httpProxy(new HttpProxy(proxyHost, proxyPort, proxyUsername, proxyPassword));
        }
        return  builder.build();
    }

    public boolean save(Context context) throws IOException {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(SHARED_PREF_KEY, new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this));
        return editor.commit();
    }

    public static SiteToSiteInfo load(Context context) throws IOException {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String string = defaultSharedPreferences.getString(SHARED_PREF_KEY, null);
        if (string == null) {
            return null;
        }
        return new ObjectMapper().readValue(string, SiteToSiteInfo.class);
    }
}
