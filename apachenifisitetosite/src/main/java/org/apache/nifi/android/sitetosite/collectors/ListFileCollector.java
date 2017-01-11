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

package org.apache.nifi.android.sitetosite.collectors;

import org.apache.nifi.android.sitetosite.DataCollector;
import org.apache.nifi.android.sitetosite.collectors.filters.AndFileFilter;
import org.apache.nifi.android.sitetosite.collectors.filters.DirectoryFileFilter;
import org.apache.nifi.android.sitetosite.collectors.filters.LastModifiedFileFilter;
import org.apache.nifi.android.sitetosite.collectors.filters.OrFileFilter;
import org.apache.nifi.android.sitetosite.packet.FileDataPacket;
import org.apache.nifi.remote.protocol.DataPacket;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * DataCollector that lists files in a directory according to a file filter and optionally modified time
 */
public class ListFileCollector implements DataCollector {
    private final File baseDir;
    private final FileFilter fileFilter;
    private boolean filterModified;
    private long minModifiedTime;

    public ListFileCollector(File baseDir, FileFilter fileFilter) {
        this.baseDir = baseDir;
        this.fileFilter = fileFilter;
        this.filterModified = false;
        this.minModifiedTime = 0L;
    }

    public ListFileCollector(File baseDir, FileFilter fileFilter, long minModifiedTime) {
        this.baseDir = baseDir;
        this.fileFilter = fileFilter;
        this.filterModified = true;
        this.minModifiedTime = minModifiedTime;
    }

    @Override
    public Iterable<DataPacket> getDataPackets() {
        long maxLastModified = System.currentTimeMillis() - 1;
        List<DataPacket> dataPackets = new ArrayList<>();
        FileFilter fileFilter;
        if (filterModified) {
            // Filter out any files not modified in window
            FileFilter modifiedCompoundFilter = new OrFileFilter(new DirectoryFileFilter(), new LastModifiedFileFilter(minModifiedTime, maxLastModified));
            fileFilter = new AndFileFilter(modifiedCompoundFilter, this.fileFilter);
        } else {
            fileFilter = this.fileFilter;
        }
        listRecursive(baseDir, fileFilter, dataPackets);
        minModifiedTime = maxLastModified + 1;
        return dataPackets;
    }

    private void listRecursive(File base, FileFilter fileFilter, List<DataPacket> output) {
        for (final File file : base.listFiles(fileFilter)) {
            if (file.isFile()) {
                output.add(new FileDataPacket(file));
            } else if (file.isDirectory()) {
                listRecursive(file, fileFilter, output);
            }
        }
    }
}
