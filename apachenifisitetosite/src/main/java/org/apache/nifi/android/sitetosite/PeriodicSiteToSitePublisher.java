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

package org.apache.nifi.android.sitetosite;

import org.apache.nifi.android.sitetosite.runnable.SiteToSitePollerRunnable;
import org.apache.nifi.remote.client.SiteToSiteClient;

/**
 * Convenience class to poll for and send data packets to via site-to-site periodically
 */
public class PeriodicSiteToSitePublisher {
    private final SiteToSiteClient siteToSiteClient;
    private final DataCollector dataCollector;
    private final PollingPolicy pollingPolicy;
    private SiteToSitePollerRunnable currentRunnable;

    public PeriodicSiteToSitePublisher(SiteToSiteClient siteToSiteClient, DataCollector dataCollector, PollingPolicy pollingPolicy) {
        this.siteToSiteClient = siteToSiteClient;
        this.dataCollector = dataCollector;
        this.pollingPolicy = pollingPolicy;
    }

    /**
     * Start polling if it hasn't already been started
     */
    public synchronized void start() {
        if (currentRunnable == null || currentRunnable.isStopped()) {
            currentRunnable = new SiteToSitePollerRunnable(siteToSiteClient, dataCollector, pollingPolicy);
            new Thread(currentRunnable).start();
        }
    }

    /**
     * Stop polling if it isn't already stopped
     */
    public synchronized void stop() {
        if (currentRunnable != null) {
            currentRunnable.stop();
            currentRunnable = null;
        }
    }

    /**
     * Returns a boolean indicating whether this poller is running
     *
     * @return a boolean indicating whether this poller is running
     */
    public synchronized boolean running() {
        return currentRunnable != null && !currentRunnable.isStopped();
    }
}
