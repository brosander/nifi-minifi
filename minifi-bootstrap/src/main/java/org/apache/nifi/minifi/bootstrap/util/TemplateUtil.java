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
import org.yaml.snakeyaml.error.YAMLException;

import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    public static final String TRANSFORM = "transform";
    public static final String VALIDATE = "validate";

    private final Map<String, Command> commandMap;
    private final PathInputStreamFactory pathInputStreamFactory;
    private final PathOutputStreamFactory pathOutputStreamFactory;

    public TemplateUtil() {
        this(FileInputStream::new, FileOutputStream::new);
    }

    public TemplateUtil(PathInputStreamFactory pathInputStreamFactory, PathOutputStreamFactory pathOutputStreamFactory) {
        this.pathInputStreamFactory = pathInputStreamFactory;
        this.pathOutputStreamFactory = pathOutputStreamFactory;
        this.commandMap = createCommandMap();
    }

    public static void main(String[] args) {
        System.exit(new TemplateUtil().execute(args));
    }

    public static void printTransformUsage() {
        System.out.println("Transform Usage:");
        System.out.println();
        System.out.print("java ");
        System.out.print(TemplateUtil.class.getCanonicalName());
        System.out.println(" transform INPUT_FILE OUTPUT_FILE");
        System.out.println();
    }

    public int validate(String[] args) {
        if (args.length != 2) {
            printTransformUsage();
            return ERR_INVALID_ARGS;
        }
        try (InputStream inputStream = pathInputStreamFactory.create(args[1])) {
            try {
                ConfigSchema configSchema = ConfigTransformer.loadConfigSchema(inputStream);
                if (!configSchema.isValid()) {
                    configSchema.getValidationIssues().forEach(s -> System.out.println(s));
                    System.out.println();
                    return ERR_INVALID_CONFIG;
                }
            } catch (InvalidConfigurationException|YAMLException e) {
                System.out.println("Unable to load configuration. (" + e + ")");
                System.out.println();
                printTransformUsage();
                return ERR_UNABLE_TO_PARSE_CONFIG;
            }
        } catch (FileNotFoundException e) {
            System.out.println("Unable to open file " + args[1] + " for reading. (" + e + ")");
            System.out.println();
            printTransformUsage();
            return ERR_UNABLE_TO_OPEN_INPUT;
        } catch (IOException e) {
            System.out.println("Error closing input. (" + e + ")");
            System.out.println();
        }

        return SUCCESS;
    }

    public int transform(String[] args) {
        if (args.length != 3) {
            printTransformUsage();
            return ERR_INVALID_ARGS;
        }
        try (InputStream inputStream = pathInputStreamFactory.create(args[1])) {
            try (OutputStream fileOutputStream = pathOutputStreamFactory.create(args[2])) {
                try {
                    ConfigTransformer.transformTemplate(inputStream, fileOutputStream);
                } catch (JAXBException e) {
                    System.out.println("Error reading template. (" + e + ")");
                    System.out.println();
                    printTransformUsage();
                    return ERR_UNABLE_TO_READ_TEMPLATE;
                } catch (IOException e) {
                    System.out.println("Error transforming template to YAML. (" + e + ")");
                    System.out.println();
                    printTransformUsage();
                    return ERR_UNABLE_TO_TRANFORM_TEMPLATE;
                }
            } catch (FileNotFoundException e) {
                System.out.println("Unable to open file " + args[2] + " for writing. (" + e + ")");
                System.out.println();
                printTransformUsage();
                return ERR_UNABLE_TO_OPEN_OUTPUT;
            } catch (IOException e) {
                System.out.println("Error closing output. (" + e + ")");
                System.out.println();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Unable to open file " + args[1] + " for reading. (" + e + ")");
            System.out.println();
            printTransformUsage();
            return ERR_UNABLE_TO_OPEN_INPUT;
        } catch (IOException e) {
            System.out.println("Error closing input. (" + e + ")");
            System.out.println();
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
        result.put(TRANSFORM, new Command(this::transform, "Transform template xml into MiNiFi config YAML"));
        result.put(VALIDATE, new Command(this::validate, "Validate config YAML"));
        return result;
    }

    public void printUsage() {
        System.out.println("Usage:");
        System.out.println();
        System.out.print("java ");
        System.out.print(TemplateUtil.class.getCanonicalName());
        System.out.println(" <command> options");
        System.out.println();
        System.out.println("Valid commands include:");
        commandMap.forEach((s, command) -> System.out.println(s + ": " + command.description));
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
