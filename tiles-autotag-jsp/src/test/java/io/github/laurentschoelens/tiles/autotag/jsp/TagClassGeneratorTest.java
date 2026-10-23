/*
 * $Id$
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.github.laurentschoelens.tiles.autotag.jsp;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import io.github.laurentschoelens.tiles.autotag.core.DirectoryOutputLocator;
import io.github.laurentschoelens.tiles.autotag.core.OutputLocator;
import io.github.laurentschoelens.tiles.autotag.core.runtime.ModelBody;
import io.github.laurentschoelens.tiles.autotag.model.TemplateClass;
import io.github.laurentschoelens.tiles.autotag.model.TemplateMethod;
import io.github.laurentschoelens.tiles.autotag.model.TemplateParameter;
import io.github.laurentschoelens.tiles.autotag.model.TemplateSuite;
import org.apache.velocity.app.VelocityEngine;
import org.junit.Test;

/**
 * Tests {@link TagClassGenerator}.
 *
 * @version $Rev$ $Date$
 */
public class TagClassGeneratorTest {

    public static final String REQUEST_CLASS = "io.github.laurentschoelens.tiles.autotag.jsp.test.Request";

    /**
     * Test method for {@link TagClassGenerator#generate(File, String, TemplateSuite, TemplateClass, Map)}.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void testGenerate() throws Exception {
        Properties props = new Properties();
        InputStream propsStream = getClass().getResourceAsStream("/io/github/laurentschoelens/tiles/autotag/velocity.properties");
        props.load(propsStream);
        propsStream.close();
        VelocityEngine velocityEngine = new VelocityEngine(props);

        TagClassGenerator generator = new TagClassGenerator(velocityEngine);
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "autotag");
        OutputLocator locator = new DirectoryOutputLocator(tempDir);
        tempDir.deleteOnExit();
        TemplateSuite suite = new TemplateSuite("tldtest", "Test for TLD docs.");
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("taglibURI", "http://www.initrode.net/tags/test");

        List<TemplateParameter> params = new ArrayList<TemplateParameter>();
        TemplateParameter param = new TemplateParameter("one", "one", "java.lang.String", null, true, false);
        param.setDocumentation("Parameter one.");
        params.add(param);
        param = new TemplateParameter("two", "two", "int", null, false, false);
        param.setDocumentation("Parameter two.");
        params.add(param);
        param = new TemplateParameter("three", "three", "boolean", null, false, false);
        param.setDocumentation("Parameter three.");
        params.add(param);
        param = new TemplateParameter("request", "request", REQUEST_CLASS, null, false, true);
        param.setDocumentation("The request.");
        params.add(param);
        param = new TemplateParameter("modelBody", "modelBody", ModelBody.class.getName(), null, false, false);
        param.setDocumentation("The body.");
        params.add(param);
        TemplateMethod executeMethod = new TemplateMethod("execute", params);

        TemplateClass clazz = new TemplateClass("io.github.laurentschoelens.tiles.autotag.template.DoStuffTemplate",
                "doStuff", "DoStuff", executeMethod);
        clazz.setDocumentation("Documentation of the DoStuff class.");

        generator.generate(locator, "io.github.laurentschoelens.tiles.autotag.jsp.test", suite, clazz, parameters,
                           "io.github.laurentschoelens.tiles.autotag.jsp.test.Runtime", REQUEST_CLASS);

        InputStream expected = getClass().getResourceAsStream("/io/github/laurentschoelens/tiles/autotag/jsp/test/DoStuffTag.java");
        File effectiveFile = new File(tempDir, "/io/github/laurentschoelens/tiles/autotag/jsp/test/DoStuffTag.java");
        assertTrue(effectiveFile.exists());
        InputStream effective = new FileInputStream(effectiveFile);
        assertEquals("Effective generated file differs from expected",
                IOUtils.toString(expected, StandardCharsets.UTF_8), IOUtils.toString(effective, StandardCharsets.UTF_8));
        effective.close();
        expected.close();

        suite.addTemplateClass(clazz);
        params = new ArrayList<TemplateParameter>();
        param = new TemplateParameter("one", "one", "java.lang.Double", null, true, false);
        param.setDocumentation("Parameter one.");
        params.add(param);
        param = new TemplateParameter("two", "two", "float", null, false, false);
        param.setDocumentation("Parameter two.");
        params.add(param);
        param = new TemplateParameter("three", "three", "java.util.Date", null, false, false);
        param.setDocumentation("Parameter three.");
        params.add(param);
        param = new TemplateParameter("request", "request", REQUEST_CLASS, null, false, true);
        param.setDocumentation("The request.");
        params.add(param);
        executeMethod = new TemplateMethod("execute", params);

        clazz = new TemplateClass("io.github.laurentschoelens.tiles.autotag.template.DoStuffNoBodyTemplate",
                "doStuffNoBody", "DoStuffNoBody", executeMethod);
        clazz.setDocumentation("Documentation of the DoStuffNoBody class.");

        suite.addTemplateClass(clazz);

        generator.generate(locator, "io.github.laurentschoelens.tiles.autotag.jsp.test", suite, clazz, parameters,
                           "io.github.laurentschoelens.tiles.autotag.jsp.test.Runtime", REQUEST_CLASS);

        expected = getClass().getResourceAsStream("/io/github/laurentschoelens/tiles/autotag/jsp/test/DoStuffNoBodyTag.java");
        effectiveFile = new File(tempDir, "/io/github/laurentschoelens/tiles/autotag/jsp/test/DoStuffNoBodyTag.java");
        assertTrue(effectiveFile.exists());
        effective = new FileInputStream(effectiveFile);
        assertTrue(IOUtils.contentEquals(effective, expected));
        effective.close();
        expected.close();

        FileUtils.deleteDirectory(tempDir);
    }

}
