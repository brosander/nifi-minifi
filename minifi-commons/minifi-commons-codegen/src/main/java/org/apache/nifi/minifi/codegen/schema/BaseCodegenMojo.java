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

package org.apache.nifi.minifi.codegen.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public abstract class BaseCodegenMojo extends AbstractMojo {
    @Parameter(defaultValue = "${basedir}/src/main/schema", property = "inputDir", required = true)
    protected File inputDir;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected final VelocityEngine velocityEngine = initVelocityEngine();

    private VelocityEngine initVelocityEngine() {
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.addProperty("resource.loader", "classpath");
        velocityEngine.addProperty("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngine.setProperty("runtime.references.strict", true);
        return velocityEngine;
    }

    protected SchemaDefinition getSchemaDefinition(File file) throws MojoExecutionException {
        SchemaDefinition schemaDefinition;
        try {
            schemaDefinition = objectMapper.readValue(file, SchemaDefinition.class);
            for (EnumDefinition enumDefinition : schemaDefinition.getEnums()) {
                enumDefinition.setParent(schemaDefinition);
            }
            for (ClassDefinition c : schemaDefinition.getClasses()) {
                c.setParent(schemaDefinition);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to load json " + file, e);
        }
        return schemaDefinition;
    }

    protected void render(String templateName, Context context, File outputFile) throws MojoExecutionException {
        Template template = velocityEngine.getTemplate(templateName);
        try (Writer writer = new FileWriter(outputFile)) {
            template.merge(context, writer);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to create file " + outputFile, e);
        }
    }
}
