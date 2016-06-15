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

import org.apache.commons.io.IOUtils;
import org.apache.nifi.controller.Template;
import org.apache.nifi.minifi.bootstrap.util.jaxb.TemplateJaxbCodec;
import org.apache.nifi.minifi.bootstrap.util.yaml.TemplateYamlCodec;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class TemplateCodecTest {

    @Test
    public void testTransformTemplate() throws IOException, JAXBException {
        TemplateJaxbCodec templateJaxbCodec = new TemplateJaxbCodec();
        TemplateYamlCodec templateYamlCodec = new TemplateYamlCodec();
        for (File file : new File("/Users/brosander/Github/nifi-templates/templates/").listFiles((dir, name) -> name.endsWith(".xml"))) {
            Template template;
            try (FileReader fileReader = new FileReader(file)) {
                template = templateJaxbCodec.read(fileReader);
            }
            String xml;
            try (FileReader fileReader = new FileReader(file)) {
                StringWriter stringWriter = new StringWriter();
                IOUtils.copy(fileReader, stringWriter);
                xml = stringWriter.toString();
            }
            StringWriter stringWriter = new StringWriter();
            templateYamlCodec.write(template, stringWriter);
            String outputFile = file.getName().substring(0, file.getName().length() - 4);
            try (FileWriter outputWriter = new FileWriter(outputFile + ".yaml")) {
                templateYamlCodec.write(template, outputWriter);
            }
            template = templateYamlCodec.read(new StringReader(stringWriter.toString()));
            stringWriter = new StringWriter();
            templateJaxbCodec.write(template, stringWriter);
            try (FileWriter outputWriter = new FileWriter(new File(file.getParent(), outputFile + ".output"))) {
                templateJaxbCodec.write(template, outputWriter);
            }
            assertEquals(file + " didn't round trip.", xml, stringWriter.toString());
        }
    }
}
