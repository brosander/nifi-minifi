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

import org.apache.nifi.remote.TransactionCompletion;

/**
 * Interface for determining when the next desired run time of the polling operation is
 */
public interface PollingPolicy {
    /**
     * Gets the next desired runtime after a successful send operation
     *
     * @param transactionCompletion information about the completed transaction
     * @return the time at which the next polling operation should occur or null if the polling should be terminated
     */
    Long getNextDesiredRuntime(TransactionCompletion transactionCompletion);

    /**
     * Gets the next desired runtime failed transaction creation
     *
     * @param consecutiveFailures the number of times this operation has failed
     * @return the time at which the attempt should occur or null if the polling should be terminated
     */
    Long getNextRetryTimeAfterFailedTransactionCreate(int consecutiveFailures);

    /**
     * Gets the next desired runtime failed send
     *
     * @param consecutiveFailures the number of times this operation has failed
     * @return the time at which the attempt should occur or null if the polling should be terminated
     */
    Long getNextRetryTimeAfterFailedSend(int consecutiveFailures);
}
