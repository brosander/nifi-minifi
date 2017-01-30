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

package com.example.myfirstapp.persistence;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Parcel;

import org.apache.nifi.android.sitetosite.service.SiteToSiteRepeatableIntent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SiteToSiteDB {
    public static final String ENTRY_SAVED = SiteToSiteDB.class.getCanonicalName() + ".save()";
    private final Context context;
    public static final String ID_COLUMN = "ID";

    public static final String S2S_TABLE_NAME = "S2S";
    public static final String S2S_CREATED_COLUMN = "CREATED";
    public static final String S2S_NUM_FILES_COLUMN = "NUM_FILES";
    public static final String S2S_RESPONSE_COLUMN = "RESPONSE";

    public static final String PENDING_INTENT_TABLE_NAME = "PENDING_INTENTS";
    public static final String PENDING_INTENT_REQUEST_CODE = "REQUEST_CODE";
    public static final String PENDING_INTENT_CONTENT_COLUMN = "CONTENT";

    public static final int VERSION = 1;

    private final SQLiteOpenHelper sqLiteOpenHelper;

    public SiteToSiteDB(Context context) {
        this.context = context;
        sqLiteOpenHelper = new SQLiteOpenHelper(context, S2S_TABLE_NAME + "_log.db", null, VERSION) {
            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE " + S2S_TABLE_NAME + " (" + ID_COLUMN + " INTEGER PRIMARY KEY, " + S2S_CREATED_COLUMN + " INTEGER, " + S2S_NUM_FILES_COLUMN + " INTEGER, " + S2S_RESPONSE_COLUMN + " TEXT)");
                db.execSQL("CREATE TABLE " + PENDING_INTENT_TABLE_NAME + " (" + ID_COLUMN + " INTEGER PRIMARY KEY, " + PENDING_INTENT_REQUEST_CODE + " INTEGER, " + PENDING_INTENT_CONTENT_COLUMN + " BLOB)");
                createPendingIntentTable(db);
            }

            private void createPendingIntentTable(SQLiteDatabase db) {
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                if (oldVersion == 1) {
                    createPendingIntentTable(db);
                }
            }
        };
    }

    public void save(TransactionLogEntry transactionLogEntry) {
        SQLiteDatabase writableDatabase = sqLiteOpenHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(S2S_CREATED_COLUMN, transactionLogEntry.getCreated().getTime());
            values.put(S2S_NUM_FILES_COLUMN, transactionLogEntry.getNumFlowFiles());
            values.put(S2S_RESPONSE_COLUMN, transactionLogEntry.getResponse());
            transactionLogEntry.setId(writableDatabase.insert(S2S_TABLE_NAME, null, values));
            context.sendBroadcast(new Intent(ENTRY_SAVED));
        } finally {
            writableDatabase.close();
        }
    }

    public List<TransactionLogEntry> getLogEntries(long lastTimestamp) {
        List<TransactionLogEntry> transactionLogEntries = new ArrayList<>();
        SQLiteDatabase readableDatabase = sqLiteOpenHelper.getReadableDatabase();
        try {
            Cursor cursor = readableDatabase.query(false, S2S_TABLE_NAME, new String[]{ID_COLUMN, S2S_CREATED_COLUMN, S2S_NUM_FILES_COLUMN, S2S_RESPONSE_COLUMN}, S2S_CREATED_COLUMN + " > ?", new String[]{Long.toString(lastTimestamp)}, null, null, "CREATED", null);
            try {
                int idIndex = cursor.getColumnIndexOrThrow(ID_COLUMN);
                int createdIndex = cursor.getColumnIndexOrThrow(S2S_CREATED_COLUMN);
                int numFilesIndex = cursor.getColumnIndexOrThrow(S2S_NUM_FILES_COLUMN);
                int responseIndex = cursor.getColumnIndexOrThrow(S2S_RESPONSE_COLUMN);
                while (cursor.moveToNext()) {
                    transactionLogEntries.add(new TransactionLogEntry(cursor.getLong(idIndex),
                            new Date(cursor.getLong(createdIndex)),
                            cursor.getInt(numFilesIndex),
                            cursor.getString(responseIndex)));
                }
            } finally {
                cursor.close();
            }
        } finally {
            readableDatabase.close();
        }
        return transactionLogEntries;
    }

    public void save(SiteToSiteRepeatableIntent siteToSiteRepeatableIntent) {
        Parcel parcel = Parcel.obtain();
        siteToSiteRepeatableIntent.getIntent().writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        byte[] bytes = parcel.marshall();
        SQLiteDatabase writableDatabase = sqLiteOpenHelper.getWritableDatabase();
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(PENDING_INTENT_REQUEST_CODE, siteToSiteRepeatableIntent.getRequestCode());
            contentValues.put(PENDING_INTENT_CONTENT_COLUMN, bytes);
            writableDatabase.insert(PENDING_INTENT_TABLE_NAME, null, contentValues);
        } finally {
            writableDatabase.close();
        }
    }

    public void deletePendingIntent(long id) {
        SQLiteDatabase writableDatabase = sqLiteOpenHelper.getWritableDatabase();
        try {
            writableDatabase.delete(PENDING_INTENT_TABLE_NAME, "ID = ?",  new String[]{Long.toString(id)});
        } finally {
            writableDatabase.close();
        }
    }

    public List<PendingIntentWrapper> getPendingIntents() {
        List<PendingIntentWrapper> pendingIntents = new ArrayList<>();
        SQLiteDatabase readableDatabase = sqLiteOpenHelper.getReadableDatabase();
        try {
            Cursor cursor = readableDatabase.query(false, PENDING_INTENT_TABLE_NAME, new String[]{ID_COLUMN, PENDING_INTENT_REQUEST_CODE, PENDING_INTENT_CONTENT_COLUMN}, null, null, null, null, null, null);
            try {
                int idIndex = cursor.getColumnIndexOrThrow(ID_COLUMN);
                int requestCodeIndex = cursor.getColumnIndexOrThrow(PENDING_INTENT_REQUEST_CODE);
                int contentIndex = cursor.getColumnIndexOrThrow(PENDING_INTENT_CONTENT_COLUMN);
                while (cursor.moveToNext()) {
                    Parcel parcel = Parcel.obtain();
                    byte[] bytes = cursor.getBlob(contentIndex);
                    parcel.unmarshall(bytes, 0, bytes.length);
                    parcel.setDataPosition(0);
                    int requestCode = cursor.getInt(requestCodeIndex);
                    Intent intent = Intent.CREATOR.createFromParcel(parcel);
                    pendingIntents.add(new PendingIntentWrapper(cursor.getLong(idIndex), PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_NO_CREATE)));
                }
            } finally {
                cursor.close();
            }
        } finally {
            readableDatabase.close();
        }
        return pendingIntents;
    }
}
