package org.apache.nifi.android.sitetosite;

import org.apache.nifi.remote.TransactionCompletion;
import org.apache.nifi.remote.protocol.DataPacket;

import java.util.Iterator;

/**
 * Created by bryan on 1/11/17.
 */

public interface DataCollector {
    Iterable<DataPacket> getDataPackets();
}
