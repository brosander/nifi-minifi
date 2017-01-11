package org.apache.nifi.android.sitetosite.collectors.filters;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by bryan on 1/11/17.
 */

public class OrFileFilter implements FileFilter {
    private final FileFilter[] delegates;

    public OrFileFilter(FileFilter... delegates) {
        this.delegates = delegates;
    }

    @Override
    public boolean accept(File pathname) {
        for (FileFilter delegate : delegates) {
            if (delegate.accept(pathname)) {
                return true;
            }
        }
        return false;
    }
}
