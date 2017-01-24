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

import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PeerTracker {
    public static final String CANONICAL_NAME = PeerTracker.class.getCanonicalName();
    private final SiteToSiteClientRequestManager siteToSiteClientRequestManager;
    private final Set<String> initialPeers;
    private final List<Peer> peerList = new ArrayList<>();

    public PeerTracker(SiteToSiteClientRequestManager siteToSiteClientRequestManager, Set<String> initialPeers) throws MalformedURLException {
        this.siteToSiteClientRequestManager = siteToSiteClientRequestManager;
        this.initialPeers = new HashSet<>(initialPeers);
        for (String initialPeer : initialPeers) {
            URL url = new URL(initialPeer);
            peerList.add(new Peer(url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + "/nifi-api"));
        }
    }

    public HttpURLConnection execute(String path) throws IOException {
        IOException lastException = null;
        for (Peer peer : peerList) {
            try {
                return siteToSiteClientRequestManager.openConnection(peer.getUrl() + path);
            } catch (IOException e) {
                Log.d(CANONICAL_NAME, e.getMessage(), e);
                lastException = e;
            }
        }
        throw lastException;
    }
}
