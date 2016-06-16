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

package org.apache.nifi.minifi.bootstrap.util.jaxb;

import org.apache.commons.io.IOUtils;
import org.apache.nifi.controller.Template;
import org.junit.Test;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.DOMDifferenceEngine;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.DifferenceEngine;
import org.xmlunit.diff.ElementSelectors;
import org.xmlunit.input.WhitespaceStrippedSource;

import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.fail;

public class TemplateJaxbCodecTest {
    @Test
    public void testTransformTemplate() throws IOException, JAXBException {
        TemplateJaxbCodec templateJaxbCodec = new TemplateJaxbCodec();
        for (File file : new File("/Users/brosander/Github/nifi-templates/templates/").listFiles((dir, name) -> name.endsWith(".xml"))) {
            String controlXml;
            try (FileReader fileReader = new FileReader(file)) {
                StringWriter stringWriter = new StringWriter();
                IOUtils.copy(fileReader, stringWriter);
                controlXml = stringWriter.toString();
            }
            Template template;
            try (StringReader stringReader = new StringReader(controlXml)) {
                template = templateJaxbCodec.read(stringReader);
            }
            StringWriter stringWriter = new StringWriter();
            templateJaxbCodec.write(template, stringWriter);
            String testXml = stringWriter.toString();

            Source control = new WhitespaceStrippedSource(Input.fromString(controlXml).build());
            Source test = new WhitespaceStrippedSource(Input.fromString(testXml).build());

            DifferenceEngine differenceEngine = new DOMDifferenceEngine();
            differenceEngine.setNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndText));
            differenceEngine.addDifferenceListener((comparison, comparisonResult) -> {
                if (comparison.getType() != ComparisonType.CHILD_NODELIST_SEQUENCE) {
                    fail("Found difference: " + comparison + " in file " + file);
                }
            });
            try {
                differenceEngine.compare(control, test);
            } catch (AssertionError assertionError) {
                try (FileWriter fileWriter = new FileWriter(new File(file.getParent(), file.getName().substring(0, file.getName().length() - 4) + ".output"))) {
                    fileWriter.write(testXml);
                }
                throw assertionError;
            }
        }
    }
}
