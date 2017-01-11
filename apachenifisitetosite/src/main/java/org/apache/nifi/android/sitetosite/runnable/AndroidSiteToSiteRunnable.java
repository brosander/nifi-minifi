package org.apache.nifi.android.sitetosite.runnable;

import org.apache.nifi.android.sitetosite.DataCollector;
import org.apache.nifi.android.sitetosite.PollingPolicy;
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
 * Created by bryan on 1/11/17.
 */

public class AndroidSiteToSiteRunnable implements Runnable {
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private final SiteToSiteClient siteToSiteClient;
    private final DataCollector dataCollector;
    private final PollingPolicy pollingPolicy;
    private final DelayProvider delayProvider;
    private final Logger logger;

    public AndroidSiteToSiteRunnable(SiteToSiteClient siteToSiteClient, DataCollector dataCollector, PollingPolicy pollingPolicy) {
        this(siteToSiteClient, dataCollector, pollingPolicy, new DelayProvider(), LoggerFactory.getLogger(AndroidSiteToSiteRunnable.class));
    }

    protected AndroidSiteToSiteRunnable(SiteToSiteClient siteToSiteClient, DataCollector dataCollector, PollingPolicy pollingPolicy, DelayProvider delayProvider, Logger logger) {
        this.siteToSiteClient = siteToSiteClient;
        this.dataCollector = dataCollector;
        this.pollingPolicy = pollingPolicy;
        this.delayProvider = delayProvider;
        this.logger = logger;
    }

    public boolean isStopped() {
        return stopped.get();
    }

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
                delayProvider.delay(Math.max(0L, nextRun - System.currentTimeMillis()));
            } catch (InterruptedException e) {
                logger.error("Interrupted while waiting for next run.", e);
                Thread.currentThread().interrupt();
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

            if (packets == null) {
                packets = dataCollector.getDataPackets();
            }

            TransactionCompletion transactionCompletion;
            try {
                for (DataPacket packet : packets) {
                    transaction.send(packet);
                }
                transaction.confirm();
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
