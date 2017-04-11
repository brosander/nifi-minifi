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
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

@Mojo(name = "schema", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class SchemaMojo extends AbstractMojo {
    @Parameter(defaultValue = "${basedir}/src/main/schema", property = "inputDir", required = true)
    private File inputDir;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/beans", property = "outputDir", required = true)
    private File outputDir;

    @Parameter(property = "project", required = true)
    private MavenProject project;

    private final VelocityEngine velocityEngine = initVelocityEngine();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private VelocityEngine initVelocityEngine() {
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.addProperty("resource.loader", "classpath");
        velocityEngine.addProperty("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngine.setProperty("runtime.references.strict", true);
        return velocityEngine;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        for (File file : inputDir.listFiles()) {
            SchemaDefinition schemaDefinition;
            try {
                schemaDefinition = objectMapper.readValue(file, SchemaDefinition.class);
                for (ClassDefinition c : schemaDefinition.getClasses()) {
                    c.setParent(schemaDefinition);
                }
            } catch (IOException e) {
                throw new MojoExecutionException("Unable to load json " + file, e);
            }
            for (ClassDefinition classDefinition : schemaDefinition.getClasses()) {
                Context context = new VelocityContext();
                context.put("class", classDefinition);
                context.put("util", new Util());
                String name = classDefinition.getName();
                if (!classDefinition.isConcrete()) {
                    name = "Abstract" + name;
                }
                render("schema.vm", context, classDefinition.getPackage(), name);
            }
        }
        project.addCompileSourceRoot(outputDir.getAbsolutePath());
    }

    private void render(String templateName, Context context, String packageName, String name) throws MojoExecutionException {
        Template template = velocityEngine.getTemplate(templateName);
        File outputFile = outputDir;
        for (String s : packageName.split("\\.")) {
            outputFile = new File(outputFile, s);
        }
        outputFile.mkdirs();
        outputFile = new File(outputFile, name + ".java");
        try (Writer writer = new FileWriter(outputFile)) {
            template.merge(context, writer);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to create file " + outputFile, e);
        }
    }
}
