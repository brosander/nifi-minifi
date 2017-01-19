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

package com.example.myfirstapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.nifi.android.sitetosite.collectors.ListFileCollector;
import org.apache.nifi.android.sitetosite.collectors.filters.RegexFileFilter;
import org.apache.nifi.android.sitetosite.packet.FileDataPacket;
import org.apache.nifi.android.sitetosite.packet.ParcelableDataPacket;
import org.apache.nifi.android.sitetosite.persist.SiteToSiteInfoBuilder;
import org.apache.nifi.android.sitetosite.service.SiteToSiteRepeating;
import org.apache.nifi.android.sitetosite.service.SiteToSiteService;
import org.apache.nifi.remote.client.KeystoreType;
import org.apache.nifi.remote.protocol.SiteToSiteTransportProtocol;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class DisplayMessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(message);

        File testFile = null;
        for (File file : getExternalMediaDirs()) {
            try {
                testFile = new File(file, "testFile");
                try (Writer outputStream = new FileWriter(testFile);) {
                    outputStream.write("hey nifi, I'm android");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        final File finalTestFile = testFile;
        AsyncTask asyncTask = new AsyncTask<String, Void, String>() {
            private Exception exception;

            @Override
            protected String doInBackground(String... params) {
                try {
                    Security.addProvider(new BouncyCastleProvider());
                    Context applicationContext = getApplicationContext();
                    new SiteToSiteInfoBuilder()
                            .setUrls(new HashSet<>(Arrays.asList("https://192.168.199.145:9443/nifi")))
                            .setPortName("input")
                            .setTransportProtocol(SiteToSiteTransportProtocol.HTTP)
                            .setKeystoreFilename("classpath:keystore.bks")
                            .setKeystorePass("dky/UyjnxapXPeNNLE3/PRGpdAnCaOOmAAWg0F1Jm3Q")
                            .setKeystoreType(KeystoreType.BKS)
                            .setTruststoreFilename("classpath:truststore.bks")
                            .setTruststorePass("Kr6ut7JD7DOxnquDhesorRAruHpRElS/lpzXWIt0e+M")
                            .setTruststoreType(KeystoreType.BKS)
                            .createSiteToSiteInfo().save(applicationContext);
                    AlarmManager alarmManager = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
//                    PendingIntent pendingIntent = SiteToSiteRepeating.createPendingIntent(applicationContext, new ListFileCollector(getExternalMediaDirs()[0], new RegexFileFilter(".*", false)), null);
                    PendingIntent pendingIntent = SiteToSiteRepeating.createPendingIntent(applicationContext, new TestDataCollector(), null);
                    alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 1000, pendingIntent);
//                    SiteToSiteService.sendDataPackets(applicationContext, new ArrayList<>(Arrays.<ParcelableDataPacket>asList(new FileDataPacket(finalTestFile))), null);
                } catch (Throwable e) {
                    System.err.println("We done failed S2S-in'");
                    e.printStackTrace();
                }

                return null;
            }
        };
        asyncTask.execute(new String[]{});


        TextView resultView = (TextView) findViewById(R.id.resultTextView);
        resultView.setText("I've sent something, here is the response: ");


        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_display_message);
        layout.addView(textView);
    }
}
