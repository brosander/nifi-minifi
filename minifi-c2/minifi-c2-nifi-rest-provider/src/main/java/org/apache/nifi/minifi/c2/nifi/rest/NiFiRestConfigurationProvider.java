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

package org.apache.nifi.minifi.c2.nifi.rest;

import com.fasterxml.jackson.core.JsonFactory;
import org.apache.nifi.minifi.c2.api.Configuration;
import org.apache.nifi.minifi.c2.api.ConfigurationProvider;
import org.apache.nifi.minifi.c2.api.ConfigurationProviderException;
import org.apache.nifi.minifi.c2.api.InvalidParameterException;
import org.apache.nifi.minifi.c2.api.cache.ConfigCache;
import org.apache.nifi.minifi.c2.api.util.Pair;
import org.apache.nifi.minifi.commons.schema.ConfigSchema;
import org.apache.nifi.minifi.commons.schema.serialization.SchemaSaver;
import org.apache.nifi.minifi.toolkit.configuration.ConfigMain;

import javax.xml.bind.JAXBException;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NiFiRestConfigurationProvider implements ConfigurationProvider {
    public static final String CONTENT_TYPE = "text/yml";
    private final JsonFactory jsonFactory = new JsonFactory();
    private final ConfigCache configCache;
    private final String nifiUrl;

    public NiFiRestConfigurationProvider(ConfigCache configCache, String nifiUrl) {
        this.configCache = configCache;
        this.nifiUrl = nifiUrl;
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    @Override
    public Configuration getConfiguration(String version, Map<String, List<String>> parameters) throws ConfigurationProviderException {
        Pair<Path, String> dirPathAndFilename = configCache.getDirPathAndFilename(parameters, false);
        String id = null;
        if (version == null) {
            Pair<String, Integer> maxIdAndVersion = getMaxIdAndVersion(dirPathAndFilename.getSecond());
            id = maxIdAndVersion.getFirst();
            version = Integer.toString(maxIdAndVersion.getSecond());
        }
        String filename = dirPathAndFilename.getSecond() + ".v" + version;
        Path parentDir = dirPathAndFilename.getFirst();
        Path cachePath = ConfigCache.resolveChildAndVerifyParent(parentDir, filename);
        if (!Files.exists(cachePath)) {
            if (id == null) {
                try {
                    Pair<Stream<Pair<String, String>>, Closeable> streamCloseablePair = getIdAndFilenameStream();
                    try {
                        id = streamCloseablePair.getFirst().filter(p -> filename.equals(p.getSecond())).map(Pair::getSecond).findFirst()
                                .orElseThrow(() -> new InvalidParameterException("Unable to find template named " + filename));
                    } finally {
                        streamCloseablePair.getSecond().close();
                    }
                } catch (IOException|TemplatesIteratorException e) {
                    throw new ConfigurationProviderException("Unable to retrieve template list", e);
                }
            }
            URL url;
            try {
                url = new URL(nifiUrl + "/templates/" + id + "/download");
            } catch (MalformedURLException e) {
                throw new ConfigurationProviderException("Unable to create url for template", e);
            }

            HttpURLConnection urlConnection;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                throw new ConfigurationProviderException("Unable to get template from url " + url, e);
            }

            try (InputStream inputStream = urlConnection.getInputStream()){
                Files.createDirectories(parentDir);
                ConfigSchema configSchema = ConfigMain.transformTemplateToSchema(inputStream);
                Path tmpPath = ConfigCache.resolveChildAndVerifyParent(parentDir, filename + UUID.randomUUID().toString());
                SchemaSaver.saveConfigSchema(configSchema, Files.newOutputStream(tmpPath));
                Files.move(tmpPath, cachePath);
            } catch (IOException e) {
                throw new ConfigurationProviderException("Unable to download template from url " + url, e);
            } catch (JAXBException e) {
                throw new ConfigurationProviderException("Unable to convert template to yaml", e);
            }
        }
        return ConfigCache.getConfiguration(cachePath, version);
    }

    private Pair<Stream<Pair<String, String>>, Closeable> getIdAndFilenameStream() throws ConfigurationProviderException, IOException {
        TemplatesIterator templatesIterator = new TemplatesIterator(nifiUrl, jsonFactory);
        return new Pair<>(StreamSupport.stream(Spliterators.spliteratorUnknownSize(templatesIterator, Spliterator.ORDERED), false), templatesIterator);
    }

    private Pair<Stream<Pair<String, Integer>>, Closeable> getIdAndVersionStream(String filename) throws ConfigurationProviderException, IOException {
        Pattern versionPattern = configCache.getVersionPattern(filename);
        Pair<Stream<Pair<String, String>>, Closeable> streamCloseablePair = getIdAndFilenameStream();
        return new Pair<>(streamCloseablePair.getFirst().map(p -> {
            Matcher matcher = versionPattern.matcher(p.getSecond());
            if (matcher.matches()) {
                return new Pair<>(p.getFirst(), Integer.parseInt(matcher.group(1)));
            }
            return null;
        }).filter(Objects::nonNull), streamCloseablePair.getSecond());
    }

    private Pair<String, Integer> getMaxIdAndVersion(String filename) throws ConfigurationProviderException {
        try {
            Pair<Stream<Pair<String, Integer>>, Closeable> streamCloseablePair = getIdAndVersionStream(filename);
            try {
                return streamCloseablePair.getFirst().sorted(Comparator.comparing(p -> ((Pair<String, Integer>) p).getSecond()).reversed()).findFirst()
                        .orElseThrow(() -> new ConfigurationProviderException("Didn't find any templates that matched " + filename + ".v[0-9]+"));
            } finally {
                streamCloseablePair.getSecond().close();
            }
        } catch (IOException|TemplatesIteratorException e) {
            throw new ConfigurationProviderException("Unable to retrieve template list", e);
        }
    }
}
