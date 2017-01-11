package org.apache.nifi.android.sitetosite;

import org.apache.nifi.android.sitetosite.runnable.AndroidSiteToSiteRunnable;
import org.apache.nifi.remote.client.SiteToSiteClient;

/**
 * Created by bryan on 1/10/17.
 */

public class AndroidSiteToSite {
    private final SiteToSiteClient siteToSiteClient;
    private final DataCollector dataCollector;
    private final PollingPolicy pollingPolicy;
    private AndroidSiteToSiteRunnable currentRunnable;

    public AndroidSiteToSite(SiteToSiteClient siteToSiteClient, DataCollector dataCollector, PollingPolicy pollingPolicy) {
        this.siteToSiteClient = siteToSiteClient;
        this.dataCollector = dataCollector;
        this.pollingPolicy = pollingPolicy;
    }

    public synchronized void start() {
        if (currentRunnable == null || currentRunnable.isStopped()) {
            currentRunnable = new AndroidSiteToSiteRunnable(siteToSiteClient, dataCollector, pollingPolicy);
            new Thread(currentRunnable).start();
        }
    }

    public synchronized void stop() {
        if (currentRunnable != null) {
            currentRunnable.stop();
            currentRunnable = null;
        }
    }

    public synchronized boolean running() {
        return currentRunnable != null && !currentRunnable.isStopped();
    }
}
