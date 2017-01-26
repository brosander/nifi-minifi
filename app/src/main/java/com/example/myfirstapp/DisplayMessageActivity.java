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

import org.apache.nifi.android.sitetosite.client.SiteToSiteClientConfig;
import org.apache.nifi.android.sitetosite.packet.DataPacket;
import org.apache.nifi.android.sitetosite.packet.EmptyDataPacket;
import org.apache.nifi.android.sitetosite.packet.FileDataPacket;
import org.apache.nifi.android.sitetosite.service.SiteToSiteRepeating;
import org.apache.nifi.android.sitetosite.service.SiteToSiteService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
                    Context applicationContext = getApplicationContext();
                    SiteToSiteClientConfig siteToSiteClientConfig = new SiteToSiteClientConfig();
//                    siteToSiteClientConfig.setUrls(new HashSet<>(Arrays.asList("http://bryan-Oryx-Pro:8080/nifi")));
                    siteToSiteClientConfig.setPortName("From Android");
                    siteToSiteClientConfig.setUrls(new HashSet<>(Arrays.asList("https://nifi-ldap.android:9443/nifi")));
                    /*siteToSiteClientConfig.setKeystoreFilename("classpath:keystore.bks");
                    siteToSiteClientConfig.setKeystorePassword("8RAIaQ/gMqgbKFkfQCRKSW9u0eP15QQeuTG7C1pB+w0");
                    siteToSiteClientConfig.setKeystoreType("BKS");*/
                    siteToSiteClientConfig.setUsername("bryan");
                    siteToSiteClientConfig.setPassword("admin-password");
                    siteToSiteClientConfig.setTruststoreFilename("classpath:truststore.bks");
                    siteToSiteClientConfig.setTruststorePassword("ks4Fx6eJWgVZ6lrBWCKY3xRGNlF6v8TlDLnMe7B8HrU");
                    siteToSiteClientConfig.setTruststoreType("BKS");
                    siteToSiteClientConfig.setProxyHost("192.168.199.145");
                    siteToSiteClientConfig.setProxyPort(3128);
                    siteToSiteClientConfig.setUseCompression(true);
//                    AlarmManager alarmManager = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
//                    PendingIntent pendingIntent = SiteToSiteRepeating.createPendingIntent(applicationContext, new ListFileCollector(getExternalMediaDirs()[0], new RegexFileFilter(".*", false)), null);
//                    PendingIntent pendingIntent = SiteToSiteRepeating.createPendingIntent(applicationContext, new TestDataCollector(), siteToSiteClientConfig, null);
//                    alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 1000, pendingIntent);
                    HashMap<String, String> attributes = new HashMap<>();
                    attributes.put("hello", "world");
                    SiteToSiteService.sendDataPackets(applicationContext, new ArrayList<>(Arrays.<DataPacket>asList(new FileDataPacket(finalTestFile), new EmptyDataPacket(attributes))), siteToSiteClientConfig, null);
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
