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
package com.jpexs.graphs.tool;

import com.jpexs.graphs.structure.BasicEditableNode;
import com.jpexs.graphs.structure.Edge;
import com.jpexs.graphs.structure.nodes.Node;
import java.util.Map;
import java.util.Set;
import com.jpexs.graphs.structure.nodes.EditableNode;

/**
 *
 * @author JPEXS
 */
public class TestOperation extends AbstractGraphOperation {

    public TestOperation(String source) {
        super(source);
    }

    @Override
    protected void executeOnMutableGraph(Set<EditableNode> nodes, Map<Node, AttributesBag> nodeAttributesMap, Map<Edge<EditableNode>, AttributesBag> edgeAttributesMap, Map<Edge<EditableNode>, String> edgeCompassesMap) {
        Node endifNode = new BasicEditableNode("endif-if");
        Node ifNode = new BasicEditableNode("if");
        Node after = new BasicEditableNode("after");
        Node ontrue = new BasicEditableNode("ontrue");

    }

}
