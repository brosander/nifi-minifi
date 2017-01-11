package org.apache.nifi.android.sitetosite;

import org.apache.nifi.remote.TransactionCompletion;

/**
 * Created by bryan on 1/11/17.
 */

public interface PollingPolicy {
    Long getNextDesiredRuntime(TransactionCompletion transactionCompletion);
    Long getNextRetryTimeAfterFailedTransactionCreate(int consecutiveFailures);
    Long getNextRetryTimeAfterFailedSend(int consecutiveFailures);
}
