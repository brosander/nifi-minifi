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

package org.apache.nifi.minifi.c2.service;

import com.wordnik.swagger.annotations.Api;
import org.apache.nifi.minifi.c2.api.Configuration;
import org.apache.nifi.minifi.c2.api.ConfigurationProvider;
import org.apache.nifi.minifi.c2.api.ConfigurationProviderException;
import org.apache.nifi.minifi.c2.api.InvalidParameterException;
import org.apache.nifi.minifi.c2.api.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/config")
@Api(
        value = "/config",
        description = "Provides configuration for MiNiFi instances"
)
public class ConfigService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);
    private final List<Pair<MediaType, ConfigurationProvider>> configurationProviders;

    public ConfigService(List<ConfigurationProvider> configurationProviders) {
        if (configurationProviders == null || configurationProviders.size() == 0) {
            throw new IllegalArgumentException("Expected at least one configuration provider");
        }
        this.configurationProviders = configurationProviders.stream().map(c -> new Pair<>(MediaType.valueOf(c.getContentType()), c)).collect(Collectors.toList());
    }

    @GET
    public Response getConfig(@Context HttpServletRequest request, @Context UriInfo uriInfo) {
        Map<String, List<String>> parameters = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : uriInfo.getQueryParameters().entrySet()) {
            parameters.put(entry.getKey(), entry.getValue());
        }
        ArrayList<String> acceptValues = Collections.list(request.getHeaders("Accept"));
        if (logger.isDebugEnabled()) {
            logger.debug("Handling request from " + getClientString(request) + " with parameters " + parameters + " and Accept: " + acceptValues.stream().collect(Collectors.joining(", ")));
        }
        Pair<MediaType, ConfigurationProvider> providerPair = getProvider(acceptValues);

        try {
            Integer version = null;
            List<String> versionList = parameters.get("version");
            if (versionList != null && versionList.size() > 0) {
                try {
                    version = Integer.parseInt(versionList.get(0));
                } catch (NumberFormatException e) {
                    throw new InvalidParameterException("Unable to parse " + version + " as integer.", e);
                }
            }
            Response.ResponseBuilder ok = Response.ok();
            Configuration configuration = providerPair.getSecond().getConfiguration(version, parameters);
            ok = ok.header("X-Content-Version", configuration.getVersion());
            ok = ok.type(providerPair.getFirst());
            byte[] buffer = new byte[1024];
            int read;
            try (InputStream inputStream = configuration.getInputStream();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                while((read = inputStream.read(buffer)) >= 0) {
                    outputStream.write(buffer, 0, read);
                    md5.update(buffer, 0, read);
                    sha256.update(buffer, 0, read);
                }
                ok = ok.header("Content-MD5", bytesToHex(md5.digest()));
                ok = ok.header("X-Content-SHA-256", bytesToHex(sha256.digest()));
                ok = ok.entity(outputStream.toByteArray());
            } catch (IOException|NoSuchAlgorithmException e) {
                logger.error("Error reading or checksumming configuration file", e);
                throw new WebApplicationException(500);
            }
            return ok.build();
        } catch (InvalidParameterException e) {
            logger.info(getClientString(request) + " made invalid request with " + getQueryString(request), e);
            return Response.status(400).build();
        } catch (ConfigurationProviderException e) {
            logger.error(getClientString(request) + " made request with " + getQueryString(request) + " that caused error in " + providerPair.getSecond(), e);
            return Response.status(500).build();
        }
    }

    // see: http://stackoverflow.com/questions/15429257/how-to-convert-byte-array-to-hexstring-in-java#answer-15429408
    protected static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    protected static String getQueryString(@Context HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString == null) {
            return "no query string";
        }
        return "query string \"" + queryString + "\"";
    }

    protected static String getClientString(@Context HttpServletRequest request) {
        String remoteHost = request.getRemoteHost();
        String remoteAddr = request.getRemoteAddr();
        String result =  "Client " + remoteHost;
        if (!remoteAddr.equals(remoteHost)) {
            result = result + " (" + remoteAddr + ")";
        }
        return result;
    }

    private Pair<MediaType, ConfigurationProvider> getProvider(List<String> acceptValues) {
        for (String accept : acceptValues) {
            MediaType acceptMediaType = MediaType.valueOf(accept);
            for (Pair<MediaType, ConfigurationProvider> configurationProviderPair : configurationProviders) {
                if (acceptMediaType.isCompatible(configurationProviderPair.getFirst())) {
                    return configurationProviderPair;
                }
            }
        }

        throw new WebApplicationException(Response.status(415).entity("Unable to find configuration provider for " +
                "\"Accept: " + acceptValues.stream().collect(Collectors.joining(", ")) + "\" supported media types are " +
                configurationProviders.stream().map(Pair::getFirst).map(Object::toString).collect(Collectors.joining(", "))).build());
    }
}