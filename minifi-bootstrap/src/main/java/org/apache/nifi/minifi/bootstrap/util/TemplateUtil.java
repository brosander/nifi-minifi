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

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public class TemplateUtil {
    public static final int ERR_INVALID_ARGS = 1;
    public static final int ERR_UNABLE_TO_OPEN_OUTPUT = 2;
    public static final int ERR_UNABLE_TO_OPEN_INPUT = 3;
    public static final int SUCCESS = 0;
    public static final int ERR_UNABLE_TO_READ_TEMPLATE = 4;
    public static final int ERR_UNABLE_TO_TRANFORM_TEMPLATE = 5;

    private final Map<String, Command> commandMap;

    public TemplateUtil() {
        this.commandMap = createCommandMap();
    }

    public static void main(String[] args) {
        System.exit(new TemplateUtil().execute(args));
    }

    public static void printTransformUsage() {

    }

    public static int transform(String[] args) {
        Reader reader;
        Writer writer;
        if (args.length < 3 || args[2].equals("-")) {
            writer = new PrintWriter(System.out);
        } else {
            try {
                writer = new FileWriter(args[2]);
            } catch (IOException e) {
                System.err.println("Unable to open file " + args[2] + " for writing. (" + e + ")");
                System.err.println();
                printTransformUsage();
                return ERR_UNABLE_TO_OPEN_OUTPUT;
            }
        }

        if (args.length < 2 || args[1].equals("-")) {
            reader = new InputStreamReader(System.in);
        } else {
            try {
                reader = new FileReader(args[1]);
            } catch (FileNotFoundException e) {
                System.err.println("Unable to open file " + args[2] + " for writing. (" + e + ")");
                System.err.println();
                printTransformUsage();
                return ERR_UNABLE_TO_OPEN_INPUT;
            }
        }

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
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                System.err.println("Error closing input. (" + e + ")");
                System.err.println();
            }
            try {
                writer.close();
            } catch (IOException e) {
                System.err.println("Error closing output. (" + e + ")");
                System.err.println();
            }
        }

        return SUCCESS;
    }

    public int execute(String[] args) {
        if (args.length < 1 || !commandMap.containsKey(args[1].toLowerCase())) {
            printUsage();
            return ERR_INVALID_ARGS;
        }
        return commandMap.get(args[1].toLowerCase()).function.apply(args);
    }

    public Map<String, Command> createCommandMap() {
        Map<String, Command> result = new TreeMap<>();
        result.put("transform", new Command(TemplateUtil::transform, "Transform template xml into MiNiFi config YAML"));
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
