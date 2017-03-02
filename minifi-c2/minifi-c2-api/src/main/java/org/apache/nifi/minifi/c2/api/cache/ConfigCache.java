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

package org.apache.nifi.minifi.c2.api.cache;

import org.apache.nifi.minifi.c2.api.Configuration;
import org.apache.nifi.minifi.c2.api.ConfigurationProviderException;
import org.apache.nifi.minifi.c2.api.HasConfiguration;
import org.apache.nifi.minifi.c2.api.InvalidParameterException;
import org.apache.nifi.minifi.c2.api.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigCache implements HasConfiguration {
    private final Path pathRoot;
    private final String pathPattern;

    public ConfigCache(String pathRoot, String pathPattern) throws IOException {
        this.pathRoot = Paths.get(pathRoot).toAbsolutePath();
        Files.createDirectories(this.pathRoot);
        this.pathPattern = pathPattern;
    }

    public static Path resolveChildAndVerifyParent(Path parent, String s) throws InvalidParameterException {
        Path child = parent.resolve(s).toAbsolutePath();
        if (child.toAbsolutePath().getParent().equals(parent)) {
            return child;
        } else {
            throw new InvalidParameterException("Path entry " + s + " not child of " + parent);
        }
    }

    public static Configuration getConfiguration(final Path path, final String version) {
        return new Configuration() {
            @Override
            public String getVersion() {
                return version;
            }

            @Override
            public InputStream getInputStream() throws ConfigurationProviderException {
                try {
                    return Files.newInputStream(path, StandardOpenOption.READ);
                } catch (IOException e) {
                    if (Files.exists(path)) {
                        throw new ConfigurationProviderException("Unable to open " + path + " for reading.", e);
                    } else {
                        throw new InvalidParameterException("File not found: " + path, e);
                    }
                }
            }
        };
    }

    public String getMostRecentVersion(Path dirPath, String fileName) throws ConfigurationProviderException {
        Pattern pattern = getVersionPattern(fileName);
        try {
            return Files.list(dirPath).map(p -> {
                Matcher matcher = pattern.matcher(p.getFileName().toString());
                if (!matcher.matches()) {
                    return null;
                }
                return new Pair<>(Integer.parseInt(matcher.group(1)), p);
            }).filter(Objects::nonNull)
                    .sorted(Comparator.comparing(pair -> ((Pair<Integer, Path>) pair).getFirst())
                            .reversed()).findFirst().orElseThrow(() -> new ConfigurationProviderException("No files in " + dirPath + " match " + pattern)).getFirst().toString();
        } catch (IOException e) {
            throw new ConfigurationProviderException("Unable to determine most recent version", e);
        }
    }

    public Pattern getVersionPattern(String fileName) {
        return Pattern.compile("^" + Pattern.quote(fileName + ".v") + "([0-9]+)$");
    }

    public Pair<Path, String> getDirPathAndFilename(Map<String, List<String>> parameters, boolean required) throws InvalidParameterException {
        String[] splitPath = substituteVariablesAndSplitPath(parameters, pathPattern);
        Path path = pathRoot.toAbsolutePath();
        for (int i = 0; i < splitPath.length - 1; i++) {
            String s = splitPath[i];
            path = resolveChildAndVerifyParent(path, s);
        }
        Path dirPath = path;
        if (!Files.exists(dirPath)) {
            if (required) {
                throw new InvalidParameterException("Calculated dir path doesn't exist: " + dirPath);
            }
        }
        return new Pair<>(dirPath, splitPath[splitPath.length - 1]);
    }

    private String[] substituteVariablesAndSplitPath(Map<String, List<String>> parameters, String pathString) throws InvalidParameterException {
        for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
            if (entry.getValue().size() != 1) {
                throw new InvalidParameterException("Multiple values for same parameter not supported in this provider.");
            }
            pathString = pathString.replaceAll(Pattern.quote("${" + entry.getKey() + "}"), entry.getValue().get(0));
        }
        String[] split = pathString.split("/");
        for (String s : split) {
            int openBrace = s.indexOf("${");
            if (openBrace >= 0 && openBrace < s.length() + 2) {
                int closeBrace = s.indexOf("}", openBrace + 2);
                if (closeBrace >= 0) {
                    throw new InvalidParameterException("Found unsubstituted variable " + s.substring(openBrace + 2, closeBrace));
                }
            }
        }
        return split;
    }

    @Override
    public Configuration getConfiguration(String version, Map<String, List<String>> parameters) throws ConfigurationProviderException {
        Pair<Path, String> dirPathAndFilename = getDirPathAndFilename(parameters, true);
        if (version == null || version.isEmpty()) {
            version = getMostRecentVersion(dirPathAndFilename.getFirst(), dirPathAndFilename.getSecond());
        }
        Path path = resolveChildAndVerifyParent(dirPathAndFilename.getFirst(), dirPathAndFilename.getSecond() + ".v" + version);

        return getConfiguration(path, version);
    }
}
