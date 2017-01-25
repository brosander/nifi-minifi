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

package org.apache.nifi.android.sitetosite.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import org.apache.nifi.android.sitetosite.client.SiteToSiteClient;
import org.apache.nifi.android.sitetosite.client.SiteToSiteClientConfig;
import org.apache.nifi.android.sitetosite.client.Transaction;
import org.apache.nifi.android.sitetosite.packet.DataPacket;
import org.apache.nifi.android.sitetosite.util.IntentUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SiteToSiteService extends IntentService {
    public static final String DATA_PACKETS = "DATA_PACKETS";
    public static final String RESULT_RECEIVER = "RESULT_RECEIVER";
    public static final String SHOULD_COMPLETE_WAKEFUL_INTENT = "SHOULD_COMPLETE_WAKEFUL_INTENT";
    public static final String SITE_TO_SITE_CONFIG = "SITE_TO_SITE_CONFIG";

    public static final int SUCCESS_RESULT_CODE = 0;
    public static final int IOE_RESULT_CODE = 1;
    public static final String CANONICAL_NAME = SiteToSiteService.class.getCanonicalName();

    public SiteToSiteService() {
        super(SiteToSiteService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, CANONICAL_NAME);
        wakeLock.acquire();

        try {
            if (intent.getBooleanExtra(SHOULD_COMPLETE_WAKEFUL_INTENT, false)) {
                WakefulBroadcastReceiver.completeWakefulIntent(intent);
            }
            List<DataPacket> packets = intent.getExtras().getParcelableArrayList(DATA_PACKETS);
            if (packets.size() > 0) {
                ResultReceiver resultReceiver = intent.getExtras().getParcelable(RESULT_RECEIVER);
                try {
                    SiteToSiteClient client = new SiteToSiteClient(IntentUtils.<SiteToSiteClientConfig>getParcelable(intent, SITE_TO_SITE_CONFIG));
                    Transaction transaction = client.createTransaction();
                    for (DataPacket packet : packets) {
                        transaction.send(packet);
                    }
                    transaction.confirm();
                    transaction.complete();
                    if (resultReceiver != null) {
                        resultReceiver.send(SUCCESS_RESULT_CODE, new Bundle());
                    }
                } catch (IOException e) {
                    Log.d(CANONICAL_NAME, "Error sending packets.", e);
                    if (resultReceiver != null) {
                        Bundle resultData = new Bundle();
                        resultData.putString("MESSAGE", e.getMessage());
                        ArrayList<String> trace = new ArrayList<>();
                        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                            trace.add(stackTraceElement.toString());
                        }
                        resultData.putStringArrayList("STACK_TRACE", trace);
                        resultReceiver.send(IOE_RESULT_CODE, resultData);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(CANONICAL_NAME, "Unexpected error sending packets.", e);
        } finally {
            wakeLock.release();
        }
    }

    public static void sendDataPackets(Context context, Iterable<DataPacket> packets, SiteToSiteClientConfig siteToSiteClientConfig, ResultReceiver resultReceiver) {
        context.startService(getIntent(context, packets, siteToSiteClientConfig, resultReceiver));
    }

    public static void sendDataPacket(Context context, DataPacket packet, SiteToSiteClientConfig siteToSiteClientConfig, ResultReceiver resultReceiver) {
        context.startService(getIntent(context, Arrays.asList(packet), siteToSiteClientConfig, resultReceiver));
    }

    public static Intent getIntent(Context context, Iterable<DataPacket> packets, SiteToSiteClientConfig siteToSiteClientConfig, ResultReceiver resultReceiver) {
        return getIntent(context, packets, siteToSiteClientConfig, resultReceiver, false);
    }

    public static Intent getIntent(Context context, Iterable<DataPacket> packets, SiteToSiteClientConfig siteToSiteClientConfig, ResultReceiver resultReceiver, boolean completeWakefulIntent) {
        Intent intent = new Intent(context, SiteToSiteService.class);
        ArrayList<DataPacket> packetList = new ArrayList<>();
        for (DataPacket packet : packets) {
            packetList.add(packet);
        }
        intent.putParcelableArrayListExtra(DATA_PACKETS, packetList);
        if (resultReceiver != null) {
            intent.putExtra(RESULT_RECEIVER, resultReceiver);
        }
        if (completeWakefulIntent) {
            intent.putExtra(SHOULD_COMPLETE_WAKEFUL_INTENT, true);
        }
        IntentUtils.putParcelable(siteToSiteClientConfig, intent, SITE_TO_SITE_CONFIG);
        return intent;
    }
}
