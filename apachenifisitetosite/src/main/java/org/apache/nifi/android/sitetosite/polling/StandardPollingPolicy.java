package org.apache.nifi.android.sitetosite.polling;

import org.apache.nifi.android.sitetosite.PollingPolicy;
import org.apache.nifi.remote.TransactionCompletion;

/**
 * Created by bryan on 1/11/17.
 */

public class StandardPollingPolicy implements PollingPolicy {
    private final int delayInMillis;

    public StandardPollingPolicy(int delayInMillis) {
        this.delayInMillis = delayInMillis;
    }

    @Override
    public Long getNextDesiredRuntime(TransactionCompletion transactionCompletion) {
        return System.currentTimeMillis() + delayInMillis;
    }

    @Override
    public Long getNextRetryTimeAfterFailedTransactionCreate(int consecutiveFailures) {
        if (consecutiveFailures > 3) {
            return null;
        }
        return System.currentTimeMillis() + delayInMillis;
    }

    @Override
    public Long getNextRetryTimeAfterFailedSend(int consecutiveFailures) {
        if (consecutiveFailures > 3) {
            return null;
        }
        return System.currentTimeMillis() + delayInMillis;
    }
}
