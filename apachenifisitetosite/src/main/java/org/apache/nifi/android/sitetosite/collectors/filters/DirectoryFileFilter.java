package org.apache.nifi.android.sitetosite.collectors.filters;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by bryan on 1/11/17.
 */

public class DirectoryFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        return pathname.isDirectory();
    }
}
