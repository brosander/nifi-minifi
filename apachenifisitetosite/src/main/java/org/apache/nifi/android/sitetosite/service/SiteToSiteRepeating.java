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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.support.v4.content.WakefulBroadcastReceiver;

import org.apache.nifi.android.sitetosite.collectors.DataCollector;
import org.apache.nifi.android.sitetosite.packet.ParcelableDataPacket;
import org.apache.nifi.android.sitetosite.util.IntentUtils;

import java.util.Random;

public class SiteToSiteRepeating extends WakefulBroadcastReceiver {
    public static final String DATA_COLLECTOR = "DATA_COLLECTOR";
    public static final String REQUEST_CODE = "REQUEST_CODE";
    private static final Random random = new Random();

    @Override
    public void onReceive(Context context, Intent intent) {
        DataCollector dataCollector = IntentUtils.getParcelable(intent, DATA_COLLECTOR);
        Iterable<ParcelableDataPacket> dataPackets = dataCollector.getDataPackets();

        // Update the pending intent with any state change in data collector
        int requestCode = getRequestCode(intent);
        ResultReceiver resultReceiver = IntentUtils.getParcelable(intent, SiteToSiteService.RESULT_RECEIVER);
        PendingIntent.getBroadcast(context, requestCode, getIntent(context, dataCollector, requestCode, resultReceiver), PendingIntent.FLAG_UPDATE_CURRENT);

        Intent packetIntent = SiteToSiteService.getIntent(context, dataPackets, resultReceiver, true);
        startWakefulService(context, packetIntent);
    }

    public synchronized static PendingIntent createPendingIntent(Context context, DataCollector dataCollector, ResultReceiver resultReceiver) {
        Intent intent = getIntent(context, dataCollector, null, resultReceiver);
        return PendingIntent.getBroadcast(context, getRequestCode(intent), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Intent getIntent(Context context, DataCollector dataCollector, Integer requestCode, ResultReceiver resultReceiver) {
        Intent intent = new Intent(context, SiteToSiteRepeating.class);
        intent.setExtrasClassLoader(dataCollector.getClass().getClassLoader());

        if (requestCode == null) {
            // Find unused requestCode
            PendingIntent existing;
            do {
                requestCode = random.nextInt();
                existing = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_NO_CREATE);
            } while (existing != null);
        }

        intent.putExtra(REQUEST_CODE, requestCode);
        IntentUtils.putParcelable(dataCollector, intent, DATA_COLLECTOR);
        IntentUtils.putParcelable(resultReceiver, intent, SiteToSiteService.RESULT_RECEIVER);
        return intent;
    }

    private static int getRequestCode(Intent intent) {
        return intent.getExtras().getInt(REQUEST_CODE);
    }
}
