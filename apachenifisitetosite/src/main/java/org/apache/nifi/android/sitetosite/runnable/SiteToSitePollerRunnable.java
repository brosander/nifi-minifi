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

package org.apache.nifi.android.sitetosite.runnable;

import org.apache.nifi.android.sitetosite.DataCollector;
import org.apache.nifi.android.sitetosite.PollingPolicy;
import org.apache.nifi.android.sitetosite.packet.DataPacketGetDataException;
import org.apache.nifi.remote.Transaction;
import org.apache.nifi.remote.TransactionCompletion;
import org.apache.nifi.remote.TransferDirection;
import org.apache.nifi.remote.client.SiteToSiteClient;
import org.apache.nifi.remote.protocol.DataPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Runnable that will poll a DataCollector for DataPackets according to its PollingPolicy and send them to Apache NiFi via site-to-site
 */
public class SiteToSitePollerRunnable implements Runnable {
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private final SiteToSiteClient siteToSiteClient;
    private final DataCollector dataCollector;
    private final PollingPolicy pollingPolicy;
    private final DelayProvider delayProvider;
    private final Logger logger;

    /**
     * Creates the runnable
     *
     * @param siteToSiteClient the client to use to send the packets
     * @param dataCollector    the collector that can retrieve packets
     * @param pollingPolicy    the policy governing polling frequency and retries
     */
    public SiteToSitePollerRunnable(SiteToSiteClient siteToSiteClient, DataCollector dataCollector, PollingPolicy pollingPolicy) {
        this(siteToSiteClient, dataCollector, pollingPolicy, new DelayProvider(), LoggerFactory.getLogger(SiteToSitePollerRunnable.class));
    }

    protected SiteToSitePollerRunnable(SiteToSiteClient siteToSiteClient, DataCollector dataCollector, PollingPolicy pollingPolicy, DelayProvider delayProvider, Logger logger) {
        this.siteToSiteClient = siteToSiteClient;
        this.dataCollector = dataCollector;
        this.pollingPolicy = pollingPolicy;
        this.delayProvider = delayProvider;
        this.logger = logger;
    }

    /**
     * Returns a boolean indicating whether the polling loop has been stopped
     *
     * @return a boolean indicating whether the polling loop has been stopped
     */
    public boolean isStopped() {
        return stopped.get();
    }

    /**
     * Stops the polling loop after its current iteration
     */
    public void stop() {
        stopped.set(true);
    }

    @Override
    public void run() {
        int consecutiveTransactionCreateFailures = 0;
        int consecutiveSendFailures = 0;
        boolean debug = logger.isDebugEnabled();
        Iterable<DataPacket> packets = null;
        long nextRun = pollingPolicy.getNextDesiredRuntime(null);
        while (!isStopped()) {
            try {
                delayProvider.delayUntil(nextRun);
            } catch (InterruptedException e) {
                logger.error("Interrupted while waiting for next run.", e);
                Thread.currentThread().interrupt();
            }

            if (packets == null) {
                packets = dataCollector.getDataPackets();
                if (!packets.iterator().hasNext()) {
                    packets = null;
                    Long nextDesiredRuntime = pollingPolicy.getNextDesiredRuntime(null);
                    if (nextDesiredRuntime == null) {
                        stop();
                    } else {
                        nextRun = nextDesiredRuntime;
                    }
                    continue;
                }
            }

            Transaction transaction;
            try {
                transaction = siteToSiteClient.createTransaction(TransferDirection.SEND);
                consecutiveTransactionCreateFailures = 0;
            } catch (IOException e) {
                logger.error("Unable to create SiteToSite transaction.", e);
                Long nextRetryTimeAfterFailedTransactionCreate = pollingPolicy.getNextRetryTimeAfterFailedTransactionCreate(++consecutiveTransactionCreateFailures);
                if (nextRetryTimeAfterFailedTransactionCreate == null) {
                    stop();
                } else {
                    nextRun = nextRetryTimeAfterFailedTransactionCreate;
                }
                continue;
            }

            TransactionCompletion transactionCompletion;
            try {
                try {
                    for (DataPacket packet : packets) {
                        transaction.send(packet);
                    }
                    transaction.confirm();
                } catch (DataPacketGetDataException e) {
                    throw e.getCause();
                }
                transactionCompletion = transaction.complete();
                consecutiveSendFailures = 0;
                packets = null;
            } catch (IOException e) {
                logger.error("Unable to send data packets.", e);
                Long nextRetryTimeAfterFailedSend = pollingPolicy.getNextRetryTimeAfterFailedSend(++consecutiveSendFailures);
                if (nextRetryTimeAfterFailedSend == null) {
                    stop();
                } else {
                    nextRun = nextRetryTimeAfterFailedSend;
                }
                continue;
            }

            Long nextDesiredRuntime = pollingPolicy.getNextDesiredRuntime(transactionCompletion);
            if (nextDesiredRuntime == null) {
                stop();
            } else {
                nextRun = nextDesiredRuntime;
            }

            if (debug) {
                StringBuilder stringBuilder = new StringBuilder("Sent ");
                stringBuilder.append(transactionCompletion.getDataPacketsTransferred());
                stringBuilder.append(" data packets (");
                stringBuilder.append(transactionCompletion.getBytesTransferred());
                stringBuilder.append(" bytes in ");
                stringBuilder.append(transactionCompletion.getDuration(TimeUnit.MILLISECONDS));
                stringBuilder.append(" milliseconds.");
                logger.debug(stringBuilder.toString());
            }
        }
    }
}
