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

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.nifi.remote.client.KeystoreType;
import org.apache.nifi.remote.client.SiteToSiteClientConfig;
import org.apache.nifi.remote.protocol.SiteToSiteTransportProtocol;
import org.apache.nifi.remote.protocol.http.HttpProxy;
import org.apache.nifi.security.util.KeyStoreUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class ParcelableSiteToSiteClientConfig implements SiteToSiteClientConfig, Parcelable {
    public static final Creator<ParcelableSiteToSiteClientConfig> CREATOR = new Creator<ParcelableSiteToSiteClientConfig>() {
        @Override
        public ParcelableSiteToSiteClientConfig createFromParcel(Parcel source) {
            ParcelableSiteToSiteClientConfig result = new ParcelableSiteToSiteClientConfig();
            List<String> urls = new ArrayList<>();
            source.readStringList(urls);
            result.urls = new HashSet<>(urls);
            result.timeoutNanos = source.readLong();
            result.penalizationPeriodNanos = source.readLong();
            result.idleConnectionExpirationNanos = source.readLong();
            result.keystoreFilename = source.readString();
            result.keystorePassword = source.readString();
            result.keystoreType = KeystoreType.valueOf(source.readString());
            result.truststoreFilename = source.readString();
            result.truststorePassword = source.readString();
            result.truststoreType = KeystoreType.valueOf(source.readString());
            result.peerPersistenceFile = new File(source.readString());
            result.useCompression = Boolean.valueOf(source.readString());
            result.transportProtocol = SiteToSiteTransportProtocol.valueOf(source.readString());
            result.portName = source.readString();
            result.portIdentifier = source.readString();
            result.preferredBatchDurationNanos = source.readLong();
            result.preferredBatchSize = source.readLong();
            result.preferredBatchCount = source.readInt();
            result.eventReporter = source.readParcelable(ParcelableSiteToSiteClientConfig.class.getClassLoader());
            result.proxyHost = source.readString();
            result.proxyPort = source.readInt();
            result.proxyUsername = source.readString();
            result.proxyPassword = source.readString();
            return result;
        }

        @Override
        public ParcelableSiteToSiteClientConfig[] newArray(int size) {
            return new ParcelableSiteToSiteClientConfig[size];
        }
    };

    private Set<String> urls;
    private long timeoutNanos = TimeUnit.SECONDS.toNanos(30);
    private long penalizationPeriodNanos = TimeUnit.SECONDS.toNanos(3);
    private long idleConnectionExpirationNanos = TimeUnit.SECONDS.toNanos(30);
    private String keystoreFilename;
    private String keystorePassword;
    private KeystoreType keystoreType;
    private String truststoreFilename;
    private String truststorePassword;
    private KeystoreType truststoreType;
    private File peerPersistenceFile;
    private boolean useCompression;
    private SiteToSiteTransportProtocol transportProtocol;
    private String portName;
    private String portIdentifier;
    private long preferredBatchDurationNanos;
    private long preferredBatchSize;
    private int preferredBatchCount;
    private ParcelableEventReporter eventReporter;
    private String proxyHost;
    private int proxyPort;
    private String proxyUsername;
    private String proxyPassword;

    public ParcelableSiteToSiteClientConfig() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(urls == null ? new ArrayList<String>() : new ArrayList<>(urls));
        dest.writeLong(timeoutNanos);
        dest.writeLong(penalizationPeriodNanos);
        dest.writeLong(idleConnectionExpirationNanos);
        dest.writeString(keystoreFilename);
        dest.writeString(keystorePassword);
        dest.writeString(keystoreType.name());
        dest.writeString(truststoreFilename);
        dest.writeString(truststorePassword);
        dest.writeString(truststoreType.name());
        dest.writeString(peerPersistenceFile.getAbsolutePath());
        dest.writeString(Boolean.toString(useCompression));
        dest.writeString(transportProtocol.name());
        dest.writeString(portName);
        dest.writeString(portIdentifier);
        dest.writeLong(preferredBatchDurationNanos);
        dest.writeLong(preferredBatchSize);
        dest.writeInt(preferredBatchCount);
        dest.writeParcelable(eventReporter, flags);
        dest.writeString(proxyHost);
        dest.writeInt(proxyPort);
        dest.writeString(proxyUsername);
        dest.writeString(proxyPassword);
    }

    @Override
    public String getUrl() {
        Set<String> urls = getUrls();
        if (urls == null || urls.size() == 0) {
            return null;
        }
        return urls.iterator().next();
    }

    @Override
    public Set<String> getUrls() {
        return urls;
    }

    public void setUrls(Set<String> urls) {
        this.urls = urls;
    }

    @Override
    public long getTimeout(TimeUnit timeUnit) {
        return timeUnit.convert(timeoutNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public long getIdleConnectionExpiration(TimeUnit timeUnit) {
        return timeUnit.convert(idleConnectionExpirationNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public long getPenalizationPeriod(TimeUnit timeUnit) {
        return timeUnit.convert(penalizationPeriodNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public SSLContext getSslContext() {
        KeyManagerFactory keyManagerFactory;

        String keystoreFilename = getKeystoreFilename();
        String keystorePassword = getKeystorePassword();
        KeystoreType keystoreType = getKeystoreType();

        if (keystoreFilename != null && keystorePassword != null && keystoreType != null) {
            try {
                keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                KeyStore keystore = KeyStoreUtils.getKeyStore(keystoreType.name());
                loadKeystore(keystore, keystoreFilename, keystorePassword);
                keyManagerFactory.init(keystore, keystorePassword.toCharArray());
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load Keystore", e);
            }
        } else {
            keyManagerFactory = null;
        }

        TrustManagerFactory trustManagerFactory;

        String truststoreFilename = getTruststoreFilename();
        String truststorePassword = getTruststorePassword();
        KeystoreType truststoreType = getTruststoreType();

        if (truststoreFilename != null && truststorePassword != null && truststoreType != null) {
            try {
                trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                KeyStore trustStore = KeyStoreUtils.getTrustStore(this.getTruststoreType().name());
                loadKeystore(trustStore, truststoreFilename, truststorePassword);
                trustManagerFactory.init(trustStore);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load Truststore", e);
            }
        } else {
            trustManagerFactory = null;
        }

        if (keyManagerFactory != null && trustManagerFactory != null) {
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
                sslContext.getDefaultSSLParameters().setNeedClientAuth(true);
                return sslContext;
            } catch (Exception e) {
                throw new IllegalStateException("Created keystore and truststore but failed to initialize SSLContext", e);
            }
        } else {
            return null;
        }
    }

    private void loadKeystore(KeyStore keystore, String filename, String password) throws IOException, GeneralSecurityException {
        Object e;
        if (filename.startsWith("classpath:")) {
            e = this.getClass().getClassLoader().getResourceAsStream(filename.substring("classpath:".length()));
        } else {
            e = new FileInputStream(filename);
        }

        try {
            keystore.load((InputStream) e, password.toCharArray());
        } finally {
            ((InputStream) e).close();
        }
    }

    @Override
    public String getKeystoreFilename() {
        return keystoreFilename;
    }

    public void setKeystoreFilename(String keystoreFilename) {
        this.keystoreFilename = keystoreFilename;
    }

    @Override
    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    @Override
    public KeystoreType getKeystoreType() {
        return keystoreType;
    }

    public void setKeystoreType(KeystoreType keystoreType) {
        this.keystoreType = keystoreType;
    }

    @Override
    public String getTruststoreFilename() {
        return truststoreFilename;
    }

    public void setTruststoreFilename(String truststoreFilename) {
        this.truststoreFilename = truststoreFilename;
    }

    @Override
    public String getTruststorePassword() {
        return truststorePassword;
    }

    public void setTruststorePassword(String truststorePassword) {
        this.truststorePassword = truststorePassword;
    }

    @Override
    public KeystoreType getTruststoreType() {
        return truststoreType;
    }

    public void setTruststoreType(KeystoreType truststoreType) {
        this.truststoreType = truststoreType;
    }

    @Override
    public File getPeerPersistenceFile() {
        return peerPersistenceFile;
    }

    public void setPeerPersistenceFile(File peerPersistenceFile) {
        this.peerPersistenceFile = peerPersistenceFile;
    }

    @Override
    public boolean isUseCompression() {
        return useCompression;
    }

    public void setUseCompression(boolean useCompression) {
        this.useCompression = useCompression;
    }

    @Override
    public SiteToSiteTransportProtocol getTransportProtocol() {
        return transportProtocol;
    }

    public void setTransportProtocol(SiteToSiteTransportProtocol transportProtocol) {
        this.transportProtocol = transportProtocol;
    }

    @Override
    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    @Override
    public String getPortIdentifier() {
        return portIdentifier;
    }

    public void setPortIdentifier(String portIdentifier) {
        this.portIdentifier = portIdentifier;
    }

    @Override
    public long getPreferredBatchDuration(TimeUnit timeUnit) {
        return timeUnit.convert(preferredBatchDurationNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public long getPreferredBatchSize() {
        return preferredBatchSize;
    }

    public void setPreferredBatchSize(long preferredBatchSize) {
        this.preferredBatchSize = preferredBatchSize;
    }

    @Override
    public int getPreferredBatchCount() {
        return preferredBatchCount;
    }

    public void setPreferredBatchCount(int preferredBatchCount) {
        this.preferredBatchCount = preferredBatchCount;
    }

    @Override
    public ParcelableEventReporter getEventReporter() {
        return eventReporter;
    }

    public void setEventReporter(ParcelableEventReporter eventReporter) {
        this.eventReporter = eventReporter;
    }

    public void setTimeoutNanos(long timeoutNanos) {
        this.timeoutNanos = timeoutNanos;
    }

    public void setPenalizationPeriodNanos(long penalizationPeriodNanos) {
        this.penalizationPeriodNanos = penalizationPeriodNanos;
    }

    public void setIdleConnectionExpirationNanos(long idleConnectionExpirationNanos) {
        this.idleConnectionExpirationNanos = idleConnectionExpirationNanos;
    }

    public void setPreferredBatchDurationNanos(long preferredBatchDurationNanos) {
        this.preferredBatchDurationNanos = preferredBatchDurationNanos;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    @Override
    public HttpProxy getHttpProxy() {
        if (proxyHost != null) {
            return new HttpProxy(proxyHost, proxyPort, proxyUsername, proxyPassword);
        }
        return null;
    }
}
