package org.apache.nifi.android.sitetosite.collectors.filters;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

/**
 * Created by bryan on 1/11/17.
 */

public class AndFileFilter implements FileFilter {
    private final FileFilter[] delegates;

    public AndFileFilter(FileFilter... delegates) {
        this.delegates = delegates;
    }

    @Override
    public boolean accept(File pathname) {
        for (FileFilter delegate : delegates) {
            if (!delegate.accept(pathname)) {
                return false;
            }
        }
        return true;
    }
}
