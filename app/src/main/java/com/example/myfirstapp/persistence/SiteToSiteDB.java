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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SiteToSiteDB {
    public static final String TABLE_NAME = "S2S";
    public static final String ID = "ID";
    public static final String CREATED = "CREATED";
    public static final String NUM_FILES = "NUM_FILES";
    public static final String RESPONSE = "RESPONSE";

    public static final int VERSION = 1;

    private final SQLiteOpenHelper sqLiteOpenHelper;

    public SiteToSiteDB(Context context) {
        sqLiteOpenHelper = new SQLiteOpenHelper(context, TABLE_NAME + "_log.db", null, VERSION) {
            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY, " + CREATED + " INTEGER, " + NUM_FILES + " INTEGER, " + RESPONSE + " TEXT)");
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            }
        };
    }

    public void save(TransactionLogEntry transactionLogEntry) {
        SQLiteDatabase writableDatabase = sqLiteOpenHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(CREATED, transactionLogEntry.getCreated().getTime());
            values.put(NUM_FILES, transactionLogEntry.getNumFlowFiles());
            values.put(RESPONSE, transactionLogEntry.getResponse());
            transactionLogEntry.setId(writableDatabase.insert(TABLE_NAME, null, values));
        } finally {
            writableDatabase.close();
        }
    }

    public List<TransactionLogEntry> getLogEntries(long lastTimestamp) {
        List<TransactionLogEntry> transactionLogEntries = new ArrayList<>();
        SQLiteDatabase readableDatabase = sqLiteOpenHelper.getReadableDatabase();
        try {
            Cursor cursor = readableDatabase.query(false, TABLE_NAME, new String[]{ID, CREATED, NUM_FILES, RESPONSE}, "CREATED > " + lastTimestamp, null, null, null, "CREATED", null);
            int idIndex = cursor.getColumnIndexOrThrow(ID);
            int createdIndex = cursor.getColumnIndexOrThrow(CREATED);
            int numFilesIndex = cursor.getColumnIndexOrThrow(NUM_FILES);
            int responseIndex = cursor.getColumnIndexOrThrow(RESPONSE);
            while (cursor.moveToNext()) {
                transactionLogEntries.add(new TransactionLogEntry(cursor.getLong(idIndex),
                        new Date(cursor.getLong(createdIndex)),
                        cursor.getInt(numFilesIndex),
                        cursor.getString(responseIndex)));
            }
        } finally {
            readableDatabase.close();
        }
        return transactionLogEntries;
    }
}
