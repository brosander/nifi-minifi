package org.apache.nifi.android.sitetosite.collectors.filters;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by bryan on 1/11/17.
 */

public class LastModifiedFileFilter implements FileFilter {
    private final long minLastModified;
    private final long maxLastModified;

    public LastModifiedFileFilter(long minLastModified, long maxLastModified) {
        this.minLastModified = minLastModified;
        this.maxLastModified = maxLastModified;
    }

    @Override
    public boolean accept(File pathname) {
        long lastModified = pathname.lastModified();
        if (lastModified >= minLastModified && lastModified <= maxLastModified) {
            return true;
        }
        return false;
    }
}
