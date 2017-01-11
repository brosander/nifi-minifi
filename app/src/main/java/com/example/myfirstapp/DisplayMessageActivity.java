package com.example.myfirstapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.nifi.android.sitetosite.AndroidSiteToSite;
import org.apache.nifi.android.sitetosite.collectors.ListFileCollector;
import org.apache.nifi.android.sitetosite.collectors.filters.RegexFileFilter;
import org.apache.nifi.android.sitetosite.polling.StandardPollingPolicy;
import org.apache.nifi.remote.Transaction;
import org.apache.nifi.remote.TransferDirection;
import org.apache.nifi.remote.client.SiteToSiteClient;
import org.apache.nifi.remote.protocol.SiteToSiteTransportProtocol;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;

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

        for (File file : getExternalMediaDirs()) {
            try {
                try(Writer outputStream = new FileWriter(new File(file, "testFile"));) {
                    outputStream.write("hey nifi, I'm android");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        AsyncTask asyncTask = new AsyncTask<String, Void, String>() {
            private Exception exception;

            @Override
            protected String doInBackground(String... params) {
                try {
                    SiteToSiteClient s2sClient = new SiteToSiteClient.Builder()
                            .url("http://192.168.199.145:8080/nifi")
                            .portName("From Android")
                            .transportProtocol(SiteToSiteTransportProtocol.HTTP)
                            .build();
                    new AndroidSiteToSite(s2sClient, new ListFileCollector(getExternalMediaDirs()[0], new RegexFileFilter(".*", false), 0L), new StandardPollingPolicy(1000)).start();
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
