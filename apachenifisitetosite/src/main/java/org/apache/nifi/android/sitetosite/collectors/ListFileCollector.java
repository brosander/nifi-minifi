package org.apache.nifi.android.sitetosite.collectors;

import org.apache.nifi.android.sitetosite.DataCollector;
import org.apache.nifi.android.sitetosite.collectors.filters.AndFileFilter;
import org.apache.nifi.android.sitetosite.collectors.filters.DirectoryFileFilter;
import org.apache.nifi.android.sitetosite.collectors.filters.LastModifiedFileFilter;
import org.apache.nifi.android.sitetosite.collectors.filters.OrFileFilter;
import org.apache.nifi.flowfile.attributes.CoreAttributes;
import org.apache.nifi.remote.protocol.DataPacket;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bryan on 1/11/17.
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
                output.add(new DataPacket() {
                    @Override
                    public Map<String, String> getAttributes() {
                        final Map<String, String> attributes = new HashMap<>();
                        attributes.put(CoreAttributes.PATH.key(), file.getParentFile().getPath());
                        attributes.put(CoreAttributes.ABSOLUTE_PATH.key(), file.getParentFile().getAbsolutePath());
                        attributes.put(CoreAttributes.FILENAME.key(), file.getName());
                        return attributes;
                    }

                    @Override
                    public InputStream getData() {
                        try {
                            return new FileInputStream(file);
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public long getSize() {
                        return file.length();
                    }
                });
            } else if (file.isDirectory()) {
                listRecursive(file, fileFilter, output);
            }
        }
    }
}
