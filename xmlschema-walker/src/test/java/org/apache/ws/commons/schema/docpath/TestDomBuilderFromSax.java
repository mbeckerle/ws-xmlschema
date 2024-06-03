/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ws.commons.schema.docpath;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.resolver.DefaultURIResolver;
import org.apache.ws.commons.schema.testutils.UtilsForTests;
import org.apache.ws.commons.schema.walker.XmlSchemaAttrInfo;
import org.apache.ws.commons.schema.walker.XmlSchemaTypeInfo;
import org.apache.ws.commons.schema.walker.XmlSchemaVisitor;
import org.apache.ws.commons.schema.walker.XmlSchemaWalker;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestDomBuilderFromSax {

    private static final File TEST_SCHEMA = UtilsForTests.buildFile("src", "test", "resources",
                                                                    "test_schema.xsd");

    private static SAXParserFactory spf;
    private static DocumentBuilderFactory dbf;

    private SAXParser saxParser;
    private DocumentBuilder domParser;

    @BeforeClass
    public static void setUpFactories() {
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
    }

    @Before
    public void setUpTest() throws Exception {
        saxParser = spf.newSAXParser();
        domParser = dbf.newDocumentBuilder();
    }

    @Test
    public void testRoot() throws Exception {
        runTest(TEST_SCHEMA, UtilsForTests.buildFile("src", "test", "resources", "test1_root.xml"));
    }

    @Test
    public void testChildren() throws Exception {
        runTest(TEST_SCHEMA, UtilsForTests.buildFile("src", "test", "resources", "test2_children.xml"));
    }

    @Test
    public void testGrandchildren() throws Exception {
        runTest(TEST_SCHEMA, UtilsForTests.buildFile("src", "test", "resources", "test3_grandchildren.xml"));
    }

    private void runTest(File schemaFile, File xmlFile) throws Exception {
        XmlSchemaCollection xmlSchemas = new XmlSchemaCollection();

        FileReader schemaFileReader = new FileReader(schemaFile);

        StreamSource schemaSource = new StreamSource(schemaFileReader, schemaFile.getName());
        xmlSchemas.read(schemaSource);

        schemaFileReader.close();

        // Parse the document using a real DOM parser
        final Document expectedDoc = domParser.parse(xmlFile);

        // Parse the document using a SAX parser
        DomBuilderFromSax builder = new DomBuilderFromSax(xmlSchemas);
        saxParser.parse(xmlFile, builder);

        final Document actualDoc = builder.getDocument();

        UtilsForTests.assertEquivalent(expectedDoc, actualDoc);
    }

    @Test
    public void testLineNumbers() throws Exception {
        String sysId = TEST_SCHEMA.toURI().toString();
        InputSource saxSource = new InputSource(sysId);
        XmlSchemaCollection xmlSchemas = DomBuilderFromSax.read(saxSource, sysId, new DefaultURIResolver());

        final TestVisitor testVisitor = new TestVisitor();
        final XmlSchemaWalker walker = new XmlSchemaWalker(xmlSchemas, testVisitor);
        final XmlSchema[] schs = xmlSchemas.getXmlSchemas();
        final XmlSchemaElement e = (XmlSchemaElement) schs[0].getElements().values().toArray()[0];
        walker.walk(e);
        assertEquals(21, testVisitor.xmlSchemaElements.size());
        testVisitor.xmlSchemaElements.forEach( xmle -> {
            assertTrue(xmle.getLineNumber() > 0 );
            assertTrue( xmle.getLinePosition() > 0);
            assertTrue( xmle.getSourceURI().endsWith(TEST_SCHEMA.toString()));
        });

    }
}

class TestVisitor implements XmlSchemaVisitor {

    public ArrayList<XmlSchemaElement> xmlSchemaElements = new ArrayList<>();

    public void onEnterElement(XmlSchemaElement element, XmlSchemaTypeInfo typeInfo, boolean previouslyVisited) {
        xmlSchemaElements.add(element);
//        System.out.println(String.format("element %s line %s col %s",
//                element.getQName().getLocalPart(),
//                element.getLineNumber(),
//                element.getLinePosition()));
    }

    public void onExitElement(XmlSchemaElement element, XmlSchemaTypeInfo typeInfo, boolean previouslyVisited) {

    }

    public void onVisitAttribute(XmlSchemaElement element, XmlSchemaAttrInfo attrInfo) {

    }

    public void onEndAttributes(XmlSchemaElement element, XmlSchemaTypeInfo typeInfo) {

    }

    public void onEnterSubstitutionGroup(XmlSchemaElement base) {

    }

    public void onExitSubstitutionGroup(XmlSchemaElement base) {

    }

    public void onEnterAllGroup(XmlSchemaAll all) {

    }

    public void onExitAllGroup(XmlSchemaAll all) {

    }

    public void onEnterChoiceGroup(XmlSchemaChoice choice) {

    }

    public void onExitChoiceGroup(XmlSchemaChoice choice) {

    }

    public void onEnterSequenceGroup(XmlSchemaSequence seq) {

    }

    public void onExitSequenceGroup(XmlSchemaSequence seq) {

    }

    public void onVisitAny(XmlSchemaAny any) {

    }

    public void onVisitAnyAttribute(XmlSchemaElement element, XmlSchemaAnyAttribute anyAttr) {

    }
}
