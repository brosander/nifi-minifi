package com.example.myfirstapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.nifi.remote.Transaction;
import org.apache.nifi.remote.TransferDirection;
import org.apache.nifi.remote.client.SiteToSiteClient;

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


        AsyncTask asyncTask = new AsyncTask<String, Void, String>() {
            private Exception exception;

            @Override
            protected String doInBackground(String... params) {
                try {
                    SiteToSiteClient s2sClient = new SiteToSiteClient.Builder()
                            .url("http://192.168.198.145:8080/nifi")
                            .portName("From Android")
                            .build();
                    final Transaction transaction = s2sClient.createTransaction(TransferDirection.SEND);
                    System.err.println("made a transaction whoo yeah");
                    transaction.send("Hello from Android".getBytes(), new HashMap<String, String>());
                    transaction.confirm();
                    transaction.complete();
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
