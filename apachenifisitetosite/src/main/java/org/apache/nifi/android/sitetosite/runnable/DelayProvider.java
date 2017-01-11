package org.apache.nifi.android.sitetosite.runnable;

/**
 * Created by bryan on 1/11/17.
 */

class DelayProvider {
    public void delay(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }
}
