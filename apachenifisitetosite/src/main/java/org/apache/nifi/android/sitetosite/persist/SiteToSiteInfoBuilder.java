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

import org.apache.nifi.remote.client.KeystoreType;
import org.apache.nifi.remote.protocol.SiteToSiteTransportProtocol;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SiteToSiteInfoBuilder {
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

    public SiteToSiteInfoBuilder setUrls(Set<String> urls) {
        this.urls = urls;
        return this;
    }

    public SiteToSiteInfoBuilder setTimeoutNanos(long timeoutNanos) {
        this.timeoutNanos = timeoutNanos;
        return this;
    }

    public SiteToSiteInfoBuilder setPenalizationNanos(long penalizationNanos) {
        this.penalizationNanos = penalizationNanos;
        return this;
    }

    public SiteToSiteInfoBuilder setIdleExpirationNanos(long idleExpirationNanos) {
        this.idleExpirationNanos = idleExpirationNanos;
        return this;
    }

    public SiteToSiteInfoBuilder setKeystoreFilename(String keystoreFilename) {
        this.keystoreFilename = keystoreFilename;
        return this;
    }

    public SiteToSiteInfoBuilder setKeystorePass(String keystorePass) {
        this.keystorePass = keystorePass;
        return this;
    }

    public SiteToSiteInfoBuilder setKeystoreType(KeystoreType keystoreType) {
        this.keystoreType = keystoreType;
        return this;
    }

    public SiteToSiteInfoBuilder setTruststoreFilename(String truststoreFilename) {
        this.truststoreFilename = truststoreFilename;
        return this;
    }

    public SiteToSiteInfoBuilder setTruststorePass(String truststorePass) {
        this.truststorePass = truststorePass;
        return this;
    }

    public SiteToSiteInfoBuilder setTruststoreType(KeystoreType truststoreType) {
        this.truststoreType = truststoreType;
        return this;
    }

    public SiteToSiteInfoBuilder setUseCompression(boolean useCompression) {
        this.useCompression = useCompression;
        return this;
    }

    public SiteToSiteInfoBuilder setTransportProtocol(SiteToSiteTransportProtocol transportProtocol) {
        this.transportProtocol = transportProtocol;
        return this;
    }

    public SiteToSiteInfoBuilder setPortName(String portName) {
        this.portName = portName;
        return this;
    }

    public SiteToSiteInfoBuilder setPortIdentifier(String portIdentifier) {
        this.portIdentifier = portIdentifier;
        return this;
    }

    public SiteToSiteInfoBuilder setBatchCount(int batchCount) {
        this.batchCount = batchCount;
        return this;
    }

    public SiteToSiteInfoBuilder setBatchSize(long batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public SiteToSiteInfoBuilder setBatchNanos(long batchNanos) {
        this.batchNanos = batchNanos;
        return this;
    }

    public SiteToSiteInfoBuilder setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
        return this;
    }

    public SiteToSiteInfoBuilder setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
        return this;
    }

    public SiteToSiteInfoBuilder setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
        return this;
    }

    public SiteToSiteInfoBuilder setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
        return this;
    }

    public SiteToSiteInfo createSiteToSiteInfo() {
        return new SiteToSiteInfo(urls, timeoutNanos, penalizationNanos, idleExpirationNanos, keystoreFilename, keystorePass, keystoreType, truststoreFilename, truststorePass, truststoreType, useCompression, transportProtocol, portName, portIdentifier, batchCount, batchSize, batchNanos, proxyHost, proxyPort, proxyUsername, proxyPassword);
    }
}