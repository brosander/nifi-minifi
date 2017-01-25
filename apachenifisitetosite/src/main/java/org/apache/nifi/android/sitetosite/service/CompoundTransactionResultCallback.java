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

import android.os.Parcel;

import org.apache.nifi.android.sitetosite.client.SiteToSiteClientConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CompoundTransactionResultCallback implements TransactionResultCallback {
    private final List<TransactionResultCallback> transactionResultCallbacks;

    public static final Creator<CompoundTransactionResultCallback> CREATOR = new Creator<CompoundTransactionResultCallback>() {
        @Override
        public CompoundTransactionResultCallback createFromParcel(Parcel source) {
            int numCallbacks = source.readInt();
            List<TransactionResultCallback> callbacks = new ArrayList<>(numCallbacks);
            for (int i = 0; i < numCallbacks; i++) {
                callbacks.add(source.<TransactionResultCallback>readParcelable(CompoundTransactionResultCallback.class.getClassLoader()));
            }
            return new CompoundTransactionResultCallback(callbacks);
        }

        @Override
        public CompoundTransactionResultCallback[] newArray(int size) {
            return new CompoundTransactionResultCallback[size];
        }
    };

    public CompoundTransactionResultCallback(List<TransactionResultCallback> transactionResultCallbacks) {
        this.transactionResultCallbacks = transactionResultCallbacks;
    }

    @Override
    public void onSuccess(SiteToSiteClientConfig siteToSiteClientConfig) {
        for (TransactionResultCallback transactionResultCallback : transactionResultCallbacks) {
            transactionResultCallback.onSuccess(siteToSiteClientConfig);
        }
    }

    @Override
    public void onException(IOException exception, SiteToSiteClientConfig siteToSiteClientConfig) {
        for (TransactionResultCallback transactionResultCallback : transactionResultCallbacks) {
            transactionResultCallback.onException(exception, siteToSiteClientConfig);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(transactionResultCallbacks.size());
        for (TransactionResultCallback transactionResultCallback : transactionResultCallbacks) {
            dest.writeParcelable(transactionResultCallback, flags);
        }
    }
}
