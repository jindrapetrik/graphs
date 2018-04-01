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
package com.jpexs.graphs.graphviz.graph.operations;

import com.jpexs.graphs.graphviz.graph.operations.codestructure.BasicDecomposedGraphOperation;
import com.jpexs.graphs.graphviz.graph.AttributesMap;
import com.jpexs.graphs.codestructure.BasicEditableNode;
import com.jpexs.graphs.codestructure.Edge;
import com.jpexs.graphs.codestructure.nodes.Node;
import java.util.Map;
import java.util.Set;
import com.jpexs.graphs.codestructure.nodes.EditableNode;
import com.jpexs.graphs.graphviz.graph.operations.codestructure.DecomposedGraph;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class TestOperation extends BasicDecomposedGraphOperation {

    @Override
    protected void executeOnDecomposedGraph(List<DecomposedGraph> decomposedGraphs, StepHandler stepHandler) {
        Node endifNode = new BasicEditableNode("endif-if");
        Node ifNode = new BasicEditableNode("if");
        Node after = new BasicEditableNode("after");
        Node ontrue = new BasicEditableNode("ontrue");

    }

}
