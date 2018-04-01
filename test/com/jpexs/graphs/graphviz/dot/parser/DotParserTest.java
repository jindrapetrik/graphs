/*
 * Copyright (C) 2018 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.graphs.graphviz.dot.parser;

import com.jpexs.graphs.graphviz.graph.SubGraph;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class DotParserTest {

    private DotParser parser;

    @BeforeTest
    public void initParser() {
        parser = new DotParser();
    }

    private void parseAllowException(String graph) throws DotParseException {
        try {
            parser.parse(new StringReader(graph));
        } catch (IOException ex) {
            Assert.fail("IOException while parsing", ex);
        }
    }

    private DotLexer lexerFor(String text) {
        return new DotLexer(new StringReader(text));
    }

    private void parse(String graph) {
        try {
            parseAllowException(graph);
        } catch (DotParseException ex) {
            Assert.fail("Exception during parsing", ex);
        }
    }

    @DataProvider(name = "provideGraphType")
    public Object[][] provideGraphType() {
        return new Object[][]{{"graph", "digraph"}};
    }

    @Test
    public void testEmptyGraph() {
        parse("graph {}");
    }

    @Test
    public void testGraphId() {
        parse("graph id{}");
    }

    @Test
    public void testDiGraph() {
        parse("digraph {}");
    }

    @Test
    public void testStrict() {
        parse("strict graph {}");
    }

    @DataProvider(name = "provideNodeSamples")
    public Object[][] provideNodeSamples() {
        return new Object[][]{
            {"n"},
            {"n[]"},
            {"n[attr1=val1]"},
            {"n[attr1=val1 attr2=val2][nattr=val3;nattr2=val4,nattr3=val5]"}
        };
    }

    @Test(dataProvider = "provideNodeSamples")
    public void testNodeStatement(String node) throws DotParseException, IOException {
        parse("graph {" + node + "}");
        new DotParser().node_stmt(new ArrayList<>(), lexerFor(node));
    }

    public Object[][] provideEdgeSamples(String edgeOp) {
        return new Object[][]{
            {"A" + edgeOp + "B"},
            {"A" + edgeOp + "B[]"},
            {"A" + edgeOp + "B[attr1=val1]"},
            {"A" + edgeOp + "B[attr1=val1 attr2=val2][nattr=val3;nattr2=val4,nattr3=val5]"},
            {"A:s" + edgeOp + "B:n"},
            {"A:X" + edgeOp + "B:Y:n"},
            {"A:X:s" + edgeOp + "B:Y"},
            {"A" + edgeOp + "B" + edgeOp + "C"}
        };
    }

    @DataProvider(name = "provideGraphEdgeSamples")
    public Object[][] provideGraphEdgeSamples() {
        return provideEdgeSamples("--");
    }

    @DataProvider(name = "provideDiGraphEdgeSamples")
    public Object[][] provideDiGraphEdgeSamples() {
        return provideEdgeSamples("->");
    }

    @Test(dataProvider = "provideGraphEdgeSamples")
    public void testGraphEdgeStatement(String edge) throws DotParseException, IOException {
        parse("graph {" + edge + "}");
        new DotParser().edge_stmt(new ArrayList<>(), new ArrayList<>(), false, lexerFor(edge));
    }

    @Test(dataProvider = "provideDiGraphEdgeSamples")
    public void testDigraphEdgeStatement(String edge) throws DotParseException, IOException {
        parse("digraph {" + edge + "}");
        new DotParser().edge_stmt(new ArrayList<>(), new ArrayList<>(), true, lexerFor(edge));
    }

    @Test
    public void testSetStatement() {
        parse("graph {x=y}");
    }

    @DataProvider(name = "provideAttrType")
    public Object[][] provideAttrType() {
        return new Object[][]{
            {"graph"},
            {"node"},
            {"edge"},};
    }

    @Test(dataProvider = "provideAttrType")
    public void testAttrStatement(String attrType) {
        parse("graph {" + attrType + "[x=y]}");
    }

    @DataProvider(name = "provideSubGraphSamples")
    public Object[][] provideSubGraphSamples() {
        return new Object[][]{
            {"subgraph{a--b--c}"},
            {"{a--b--c}"},
            {"subgraph gr1{a--b--c}"},
            {"{a--b--c}--{d--e--f}"},
            {"{a--b--c}--subgraph{d--e--f}"}
        };
    }

    @Test(dataProvider = "provideSubGraphSamples")
    public void testSubGraph(String subGraph) {
        parse("graph {" + subGraph + "}");
    }

    @Test
    public void testStatementListSemicolon() {
        parse("graph {a;a--b;c}");
    }

    @Test
    public void testGraphCannotUseArrowEdgeOp() throws IOException, DotParseException {
        String sample = "a->b";
        try {
            parseAllowException("graph {" + sample + "}");
            Assert.fail("-> for digraph allowed");
        } catch (DotParseException ex) {
            //okay
        }

        try {
            new DotParser().edge_stmt(new ArrayList<>(), new ArrayList<>(), false, lexerFor(sample));
            Assert.fail("-> for graph allowed");
        } catch (DotParseException ex) {
            //okay
        }
    }

    @Test
    public void testDiGraphCannotUseMinusMinusEdgeOp() throws IOException, DotParseException {
        String sample = "a--b";
        try {
            parseAllowException("digraph {" + sample + "}");
            Assert.fail("-- for digraph allowed");
        } catch (DotParseException ex) {
            //okay
        }

        try {
            new DotParser().edge_stmt(new ArrayList<>(), new ArrayList<>(), true, lexerFor(sample));
            Assert.fail("-- for digraph allowed");
        } catch (DotParseException ex) {
            //okay
        }
    }

}
