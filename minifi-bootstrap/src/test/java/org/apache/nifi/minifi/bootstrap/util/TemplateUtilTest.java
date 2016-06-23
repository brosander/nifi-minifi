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

import org.apache.commons.io.Charsets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TemplateUtilTest {
    @Mock
    PathInputStreamFactory pathInputStreamFactory;

    @Mock
    PathOutputStreamFactory pathOutputStreamFactory;

    TemplateUtil templateUtil;

    String testInput;

    String testOutput;

    @Before
    public void setup() {
        templateUtil = new TemplateUtil(pathInputStreamFactory, pathOutputStreamFactory);
        testInput = "testInput";
        testOutput = "testOutput";
    }

    @Test
    public void testExecuteNoArgs() {
        assertEquals(TemplateUtil.ERR_INVALID_ARGS, templateUtil.execute(new String[0]));
    }

    @Test
    public void testExecuteInvalidCommand() {
        assertEquals(TemplateUtil.ERR_INVALID_ARGS, templateUtil.execute(new String[]{"badCommand"}));
    }

    @Test
    public void testValidateInvalidCommand() {
        assertEquals(TemplateUtil.ERR_INVALID_ARGS, templateUtil.execute(new String[]{TemplateUtil.VALIDATE}));
    }

    @Test
    public void testValidateErrorOpeningInput() throws FileNotFoundException {
        when(pathInputStreamFactory.create(testInput)).thenThrow(new FileNotFoundException());
        assertEquals(TemplateUtil.ERR_UNABLE_TO_OPEN_INPUT, templateUtil.execute(new String[]{TemplateUtil.VALIDATE, testInput}));
    }

    @Test
    public void testValidateUnableToParseConfig() throws FileNotFoundException {
        when(pathInputStreamFactory.create(testInput)).thenReturn(new ByteArrayInputStream("!@#$%^&".getBytes(Charsets.UTF_8)));
        assertEquals(TemplateUtil.ERR_UNABLE_TO_PARSE_CONFIG, templateUtil.execute(new String[]{TemplateUtil.VALIDATE, testInput}));
    }

    @Test
    public void testValidateInvalidConfig() throws FileNotFoundException {
        when(pathInputStreamFactory.create(testInput)).thenAnswer(invocation ->
                TemplateUtilTest.class.getClassLoader().getResourceAsStream("config-malformed-field.yml"));
        assertEquals(TemplateUtil.ERR_INVALID_CONFIG, templateUtil.execute(new String[]{TemplateUtil.VALIDATE, testInput}));
    }

    @Test
    public void testTransformInvalidCommand() {
        assertEquals(TemplateUtil.ERR_INVALID_ARGS, templateUtil.execute(new String[]{TemplateUtil.TRANSFORM}));
    }

    @Test
    public void testValidateSuccess() throws FileNotFoundException {
        when(pathInputStreamFactory.create(testInput)).thenAnswer(invocation ->
                TemplateUtilTest.class.getClassLoader().getResourceAsStream("config.yml"));
        assertEquals(TemplateUtil.SUCCESS, templateUtil.execute(new String[]{TemplateUtil.VALIDATE, testInput}));
    }

    @Test
    public void testTransformErrorOpeningInput() throws FileNotFoundException {
        when(pathInputStreamFactory.create(testInput)).thenThrow(new FileNotFoundException());
        assertEquals(TemplateUtil.ERR_UNABLE_TO_OPEN_INPUT, templateUtil.execute(new String[]{TemplateUtil.TRANSFORM, testInput, testOutput}));
    }

    @Test
    public void testTransformErrorOpeningOutput() throws FileNotFoundException {
        when(pathOutputStreamFactory.create(testOutput)).thenThrow(new FileNotFoundException());
        assertEquals(TemplateUtil.ERR_UNABLE_TO_OPEN_OUTPUT, templateUtil.execute(new String[]{TemplateUtil.TRANSFORM, testInput, testOutput}));
    }

    @Test
    public void testTransformErrorReadingTemplate() throws FileNotFoundException {
        when(pathInputStreamFactory.create(testInput)).thenAnswer(invocation -> new ByteArrayInputStream("malformed xml".getBytes(Charsets.UTF_8)));
        assertEquals(TemplateUtil.ERR_UNABLE_TO_READ_TEMPLATE, templateUtil.execute(new String[]{TemplateUtil.TRANSFORM, testInput, testOutput}));
    }

    @Test
    public void testTransformErrorTransformingTemplate() throws FileNotFoundException {
        when(pathInputStreamFactory.create(testInput)).thenAnswer(invocation ->
                TemplateUtilTest.class.getClassLoader().getResourceAsStream("Working_with_Logs.xml"));
        when(pathOutputStreamFactory.create(testOutput)).thenAnswer(invocation -> new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException();
            }
        });
        assertEquals(TemplateUtil.ERR_UNABLE_TO_TRANFORM_TEMPLATE, templateUtil.execute(new String[]{TemplateUtil.TRANSFORM, testInput, testOutput}));
    }

    @Test
    public void testTransformSuccess() throws FileNotFoundException {
        when(pathInputStreamFactory.create(testInput)).thenAnswer(invocation ->
                TemplateUtilTest.class.getClassLoader().getResourceAsStream("Working_with_Logs.xml"));
        when(pathOutputStreamFactory.create(testOutput)).thenAnswer(invocation -> new ByteArrayOutputStream());
        assertEquals(TemplateUtil.SUCCESS, templateUtil.execute(new String[]{TemplateUtil.TRANSFORM, testInput, testOutput}));
    }
}
