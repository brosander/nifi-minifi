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

import java.util.Date;

public class TransactionLogEntry {
    private long id;
    private final Date created;
    private final int numFlowFiles;
    private final String response;

    public TransactionLogEntry(Date created, int numFlowFiles, String response) {
        this(-1, created, numFlowFiles, response);
    }

    public TransactionLogEntry(long id, Date created, int numFlowFiles, String response) {
        this.id = id;
        this.created = created;
        this.numFlowFiles = numFlowFiles;
        this.response = response;
    }

    public long getId() {
        return id;
    }

    public Date getCreated() {
        return created;
    }

    public int getNumFlowFiles() {
        return numFlowFiles;
    }

    public String getResponse() {
        return response;
    }

    protected void setId(long id) {
        this.id = id;
    }
}
