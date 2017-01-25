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

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import org.apache.nifi.android.sitetosite.client.SiteToSiteClientConfig;

import java.io.IOException;

public class SiteToSiteResultReceiver extends ResultReceiver implements TransactionResultCallback {
    public static final String IO_EXCEPTION = "IOException";

    private final TransactionResultCallback delegate;
    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */
    public SiteToSiteResultReceiver(Handler handler, TransactionResultCallback delegate) {
        super(handler);
        this.delegate = delegate;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);
        SiteToSiteClientConfig siteToSiteClientConfig = resultData.getParcelable(SiteToSiteService.SITE_TO_SITE_CONFIG);
        if (resultCode == 0) {
            delegate.onSuccess(siteToSiteClientConfig);
        } else {
            delegate.onException((IOException)resultData.getSerializable(IO_EXCEPTION), siteToSiteClientConfig);
        }
    }

    @Override
    public void onSuccess(SiteToSiteClientConfig siteToSiteClientConfig) {
        Bundle resultData = new Bundle();
        resultData.putParcelable(SiteToSiteService.SITE_TO_SITE_CONFIG, siteToSiteClientConfig);
        send(0, resultData);
    }

    @Override
    public void onException(IOException exception, SiteToSiteClientConfig siteToSiteClientConfig) {
        Bundle resultData = new Bundle();
        resultData.putParcelable(SiteToSiteService.SITE_TO_SITE_CONFIG, siteToSiteClientConfig);
        resultData.putSerializable(IO_EXCEPTION, exception);
        send(1, resultData);
    }
}
