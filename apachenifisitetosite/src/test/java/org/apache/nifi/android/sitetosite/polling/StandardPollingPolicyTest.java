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

import org.apache.nifi.remote.TransactionCompletion;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class StandardPollingPolicyTest {
    @Mock
    private TransactionCompletion transactionCompletion;

    private int delay;

    private int retries;

    private StandardPollingPolicy standardPollingPolicy;

    @Before
    public void setup() {
        delay = 500;
        retries = 5;
        standardPollingPolicy = new StandardPollingPolicy(delay, retries);
    }

    @Test
    public void testGetNextDesiredRuntime() {
        long before = System.currentTimeMillis();
        Long nextDesiredRuntime = standardPollingPolicy.getNextDesiredRuntime(transactionCompletion);
        long after = System.currentTimeMillis();

        assertTrue(nextDesiredRuntime >= before + delay);
        assertTrue(nextDesiredRuntime <= after + delay);
    }

    @Test
    public void testGetNextRetryTimeAfterFailedTransactionCreate() {
        for (int i = 1; i <= retries; i++) {
            long before = System.currentTimeMillis();
            Long nextDesiredRuntime = standardPollingPolicy.getNextRetryTimeAfterFailedTransactionCreate(retries);
            long after = System.currentTimeMillis();

            assertTrue(nextDesiredRuntime >= before + delay);
            assertTrue(nextDesiredRuntime <= after + delay);
        }
        assertNull(standardPollingPolicy.getNextRetryTimeAfterFailedTransactionCreate(retries + 1));
    }

    @Test
    public void testGetNextRetryTimeAfterFailedFailedSend() {
        for (int i = 1; i <= retries; i++) {
            long before = System.currentTimeMillis();
            Long nextDesiredRuntime = standardPollingPolicy.getNextRetryTimeAfterFailedSend(retries);
            long after = System.currentTimeMillis();

            assertTrue(nextDesiredRuntime >= before + delay);
            assertTrue(nextDesiredRuntime <= after + delay);
        }
        assertNull(standardPollingPolicy.getNextRetryTimeAfterFailedSend(retries + 1));
    }
}
