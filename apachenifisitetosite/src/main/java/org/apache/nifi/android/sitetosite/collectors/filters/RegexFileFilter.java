package org.apache.nifi.android.sitetosite.collectors.filters;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

/**
 * Created by bryan on 1/11/17.
 */

public class RegexFileFilter implements FileFilter {
    private final Pattern pattern;
    private final boolean matchAbsolutePath;

    public RegexFileFilter(String regex, boolean matchAbsolutePath) {
        this.pattern = Pattern.compile(regex);
        this.matchAbsolutePath = matchAbsolutePath;
    }

    @Override
    public boolean accept(File pathname) {
        String pathToMatch = matchAbsolutePath ? pathname.getAbsolutePath() : pathname.getName();
        return pattern.matcher(pathToMatch).matches();
    }
}
