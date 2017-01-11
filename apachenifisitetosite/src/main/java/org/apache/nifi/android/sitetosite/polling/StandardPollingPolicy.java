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

package org.apache.nifi.android.sitetosite.polling;

import org.apache.nifi.android.sitetosite.PollingPolicy;
import org.apache.nifi.remote.TransactionCompletion;

/**
 * Polling policy that always returns the same delay in millis and has a retry limit
 */
public class StandardPollingPolicy implements PollingPolicy {
    private final int delayInMillis;
    private final int maxRetries;

    public StandardPollingPolicy(int delayInMillis, int maxRetries) {
        this.delayInMillis = delayInMillis;
        this.maxRetries = maxRetries;
    }

    @Override
    public Long getNextDesiredRuntime(TransactionCompletion transactionCompletion) {
        return System.currentTimeMillis() + delayInMillis;
    }

    @Override
    public Long getNextRetryTimeAfterFailedTransactionCreate(int consecutiveFailures) {
        if (consecutiveFailures >= maxRetries) {
            return null;
        }
        return System.currentTimeMillis() + delayInMillis;
    }

    @Override
    public Long getNextRetryTimeAfterFailedSend(int consecutiveFailures) {
        if (consecutiveFailures >= maxRetries) {
            return null;
        }
        return System.currentTimeMillis() + delayInMillis;
    }
}
