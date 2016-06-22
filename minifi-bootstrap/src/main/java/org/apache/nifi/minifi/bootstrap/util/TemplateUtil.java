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

package org.apache.nifi.minifi.bootstrap.util;

import org.apache.nifi.minifi.bootstrap.exception.InvalidConfigurationException;
import org.apache.nifi.minifi.bootstrap.util.schema.ConfigSchema;

import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public class TemplateUtil {
    public static final int ERR_INVALID_ARGS = 1;
    public static final int ERR_UNABLE_TO_OPEN_OUTPUT = 2;
    public static final int ERR_UNABLE_TO_OPEN_INPUT = 3;
    public static final int ERR_UNABLE_TO_READ_TEMPLATE = 4;
    public static final int ERR_UNABLE_TO_TRANFORM_TEMPLATE = 5;
    public static final int ERR_UNABLE_TO_PARSE_CONFIG = 6;
    public static final int ERR_INVALID_CONFIG = 7;

    public static final int SUCCESS = 0;

    private final Map<String, Command> commandMap;

    public TemplateUtil() {
        this.commandMap = createCommandMap();
    }

    public static void main(String[] args) {
        System.exit(new TemplateUtil().execute(args));
    }

    public static void printTransformUsage() {
        System.err.println("Transform Usage:");
        System.err.println();
        System.err.print("java ");
        System.err.print(TemplateUtil.class.getCanonicalName());
        System.err.println(" transform INPUT_FILE OUTPUT_FILE");
        System.err.println();
    }

    public static int validate(String[] args) {
        if (args.length != 2) {
            printTransformUsage();
            return ERR_INVALID_ARGS;
        }
        try (InputStream inputStream = new FileInputStream(args[1])) {
            try {
                ConfigSchema configSchema = ConfigTransformer.loadConfigSchema(inputStream);
                if (!configSchema.isValid()) {
                    configSchema.getValidationIssues().forEach(s -> System.err.println(s));
                }
                System.err.println();
                return ERR_INVALID_CONFIG;
            } catch (InvalidConfigurationException e) {
                System.err.println("Unable to load configuration. (" + e + ")");
                System.err.println();
                printTransformUsage();
                return ERR_UNABLE_TO_PARSE_CONFIG;
            }
        } catch (FileNotFoundException e) {
            System.err.println("Unable to open file " + args[1] + " for reading. (" + e + ")");
            System.err.println();
            printTransformUsage();
            return ERR_UNABLE_TO_OPEN_INPUT;
        } catch (IOException e) {
            System.err.println("Error closing input. (" + e + ")");
            System.err.println();
        }

        return SUCCESS;
    }

    public static int transform(String[] args) {
        if (args.length != 3) {
            printTransformUsage();
            return ERR_INVALID_ARGS;
        }
        try (Reader reader = new FileReader(args[1])) {
            try (Writer writer = new FileWriter(args[2])) {
                try {
                    ConfigTransformer.transformTemplate(reader, writer);
                } catch (JAXBException e) {
                    System.err.println("Error reading template. (" + e + ")");
                    System.err.println();
                    printTransformUsage();
                    return ERR_UNABLE_TO_READ_TEMPLATE;
                } catch (IOException e) {
                    System.err.println("Error transforming template to YAML. (" + e + ")");
                    System.err.println();
                    printTransformUsage();
                    return ERR_UNABLE_TO_TRANFORM_TEMPLATE;
                }
            } catch (FileNotFoundException e) {
                System.err.println("Unable to open file " + args[2] + " for writing. (" + e + ")");
                System.err.println();
                printTransformUsage();
                return ERR_UNABLE_TO_OPEN_OUTPUT;
            } catch (IOException e) {
                System.err.println("Error closing output. (" + e + ")");
                System.err.println();
            }
        } catch (FileNotFoundException e) {
            System.err.println("Unable to open file " + args[1] + " for reading. (" + e + ")");
            System.err.println();
            printTransformUsage();
            return ERR_UNABLE_TO_OPEN_INPUT;
        } catch (IOException e) {
            System.err.println("Error closing input. (" + e + ")");
            System.err.println();
        }

        return SUCCESS;
    }

    public int execute(String[] args) {
        if (args.length < 1 || !commandMap.containsKey(args[0].toLowerCase())) {
            printUsage();
            return ERR_INVALID_ARGS;
        }
        return commandMap.get(args[0].toLowerCase()).function.apply(args);
    }

    public Map<String, Command> createCommandMap() {
        Map<String, Command> result = new TreeMap<>();
        result.put("transform", new Command(TemplateUtil::transform, "Transform template xml into MiNiFi config YAML"));
        result.put("validate", new Command(TemplateUtil::validate, "Validate config YAML"));
        return result;
    }

    public void printUsage() {
        System.err.println("Usage:");
        System.err.println();
        System.err.print("java ");
        System.err.print(TemplateUtil.class.getCanonicalName());
        System.err.println(" <command> options");
        System.err.println();
        System.err.println("Valid commands include:");
        commandMap.forEach((s, command) -> System.err.println(s + ": " + command.description));
    }

    public class Command {
        private final Function<String[], Integer> function;
        private final String description;

        public Command(Function<String[], Integer> function, String description) {
            this.function = function;
            this.description = description;
        }
    }
}
