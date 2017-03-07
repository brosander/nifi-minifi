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

package org.apache.nifi.minifi.c2.provider.nifi.rest;

import org.apache.nifi.minifi.c2.api.ConfigurationProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class NiFiRestConnector {
    private static final Logger logger = LoggerFactory.getLogger(NiFiRestConnector.class);

    private final String nifiApiUrl;

    public NiFiRestConnector(String nifiApiUrl) {
        this.nifiApiUrl = nifiApiUrl;
    }

    protected HttpURLConnection get(String endpointPath) throws ConfigurationProviderException {
        String endpointUrl = nifiApiUrl + endpointPath;
        if (logger.isDebugEnabled()) {
            logger.debug("Connecting to NiFi endpoint: " + endpointUrl);
        }
        URL url;
        try {
            url = new URL(endpointUrl);
        } catch (MalformedURLException e) {
            throw new ConfigurationProviderException("Malformed url " + endpointUrl, e);
        }

        try {
            return (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new ConfigurationProviderException("Unable to connect to " + url, e);
        }
    }
}
