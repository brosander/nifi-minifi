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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;

import java.io.File;

@Mojo(name = "schema", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class SchemaMojo extends BaseCodegenMojo {
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/beans", property = "outputDir", required = true)
    private File outputDir;

    @Parameter(property = "project", required = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        for (File file : inputDir.listFiles()) {
            SchemaDefinition schemaDefinition = getSchemaDefinition(file);
            for (EnumDefinition enumDefinition : schemaDefinition.getEnums()) {
                Context context = new VelocityContext();
                context.put("enum", enumDefinition);
                context.put("schema", new SchemaUtil());
                context.put("util", new Util());
                render("enum.vm", context, getOutputFile(schemaDefinition.getPackage(), enumDefinition.getName()));
            }
            for (ClassDefinition classDefinition : schemaDefinition.getClasses()) {
                Context context = new VelocityContext();
                context.put("class", classDefinition);
                context.put("schema", new SchemaUtil());
                context.put("util", new Util());
                String name = classDefinition.getName();
                if (!classDefinition.isConcrete()) {
                    name = "Abstract" + name;
                }
                render("schema.vm", context, getOutputFile(classDefinition.getPackage(), name));
            }
        }
        project.addCompileSourceRoot(outputDir.getAbsolutePath());
    }

    private File getOutputFile(String packageName, String name) {
        File outputFile = outputDir;
        for (String s : packageName.split("\\.")) {
            outputFile = new File(outputFile, s);
        }
        outputFile.mkdirs();
        outputFile = new File(outputFile, name + ".java");
        return outputFile;
    }
}
