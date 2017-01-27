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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.apache.nifi.android.sitetosite.client.SiteToSiteClientConfig;
import org.apache.nifi.android.sitetosite.packet.ByteArrayDataPacket;
import org.apache.nifi.android.sitetosite.service.SiteToSiteService;
import org.apache.nifi.android.sitetosite.service.TransactionResultCallback;
import org.apache.nifi.android.sitetosite.util.Charsets;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.preferences:
                Intent intent = new Intent();
                intent.setClassName(this, SiteToSitePreferenceActivity.class.getCanonicalName());
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when the user clicks the Send button
     */
    public void sendMessage(View view) {
        Map<String, String> attributes = new HashMap<>();
        ByteArrayDataPacket dataPacket = new ByteArrayDataPacket(attributes, ((EditText) findViewById(R.id.edit_message)).getText().toString().getBytes(Charsets.UTF_8));
        SiteToSiteService.sendDataPacket(getApplicationContext(), dataPacket, getClientConfig(), new TransactionResultCallback() {

            @Override
            public Handler getHandler() {
                return handler;
            }

            @Override
            public void onSuccess(SiteToSiteClientConfig siteToSiteClientConfig) {
                append("I've sent a message!");
            }

            @Override
            public void onException(IOException exception, SiteToSiteClientConfig siteToSiteClientConfig) {
                append(exception.getMessage());
            }
        });
    }

    private SiteToSiteClientConfig getClientConfig() {
        SiteToSiteClientConfig siteToSiteClientConfig = new SiteToSiteClientConfig();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        siteToSiteClientConfig.setUrls(new HashSet<>(Arrays.asList(preferences.getString("peer_urls_preference", "http://localhost:8080/nifi"))));
        siteToSiteClientConfig.setPortName(preferences.getString("input_port_preference", null));
        siteToSiteClientConfig.setUsername(preferences.getString("username_preference", null));
        siteToSiteClientConfig.setPassword(preferences.getString("password_preference", null));
        siteToSiteClientConfig.setProxyHost(preferences.getString("proxy_host_preference", null));
        siteToSiteClientConfig.setProxyPort(Integer.parseInt(preferences.getString("proxy_port_preference", "0")));
        siteToSiteClientConfig.setProxyUsername(preferences.getString("proxy_port_username", null));
        siteToSiteClientConfig.setProxyPassword(preferences.getString("proxy_port_password", null));
        siteToSiteClientConfig.setProxyPassword(preferences.getString("proxy_port_password", null));
        return siteToSiteClientConfig;
    }

    private void append(String text) {
        TextView resultView = (TextView) findViewById(R.id.sendResults);
        int lineCount = resultView.getLineCount();
        CharSequence resultViewText = resultView.getText();
        if (lineCount == 1 && resultViewText.equals("Send result")) {
            resultView.setText("");
        }
        trimToLineCount(resultView, 100);

        resultView.append("[" + simpleDateFormat.format(new Date()) + "] - " + text + System.lineSeparator());

        final ScrollView scroll = (ScrollView) findViewById(R.id.scrollView);
        scroll.post(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void trimToLineCount(TextView resultView, int lineCount) {
        CharSequence resultViewText = resultView.getText();
        String lineSeparator = System.lineSeparator();
        int lineSepLength = lineSeparator.length();
        while (resultView.getLineCount() > lineCount) {
            int length = resultViewText.length() + 1 - lineSepLength;
            for (int i = 0; i < length; i++) {
                boolean match = true;
                for (int o = 0; o < lineSepLength; o++) {
                    if (resultViewText.charAt(i + o) != lineSeparator.charAt(o)) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    resultView.setText(resultViewText.subSequence(i + 1, resultViewText.length()));
                    break;
                }
            }
        }
    }
}
