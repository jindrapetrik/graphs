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
package com.jpexs.graphs.graphviz.graph.operations.codestructure;

import com.jpexs.graphs.graphviz.dot.parser.DotParseException;
import com.jpexs.graphs.graphviz.dot.parser.DotParser;
import com.jpexs.graphs.graphviz.graph.AttributesMap;
import com.jpexs.graphs.graphviz.graph.Graph;
import com.jpexs.graphs.graphviz.graph.operations.GraphOperation;
import com.jpexs.graphs.graphviz.graph.operations.StepHandler;
import com.jpexs.graphs.graphviz.graph.operations.StringOperation;
import com.jpexs.graphs.codestructure.Edge;
import com.jpexs.graphs.codestructure.nodes.EditableNode;
import com.jpexs.graphs.codestructure.nodes.Node;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public abstract class BasicDecomposedGraphOperation implements StringOperation, GraphOperation {

    protected StructuredGraphFacade facade;

    public BasicDecomposedGraphOperation() {
        facade = new StructuredGraphFacade();
    }

    protected String nodesToString(String join, Collection<Node> nodes) {
        List<String> strs = new ArrayList<>();
        for (Node n : nodes) {
            strs.add(n.toString());
        }
        return String.join(join, strs);
    }

    protected abstract void executeOnDecomposedGraph(Set<EditableNode> nodes, Map<Node, AttributesMap> nodeAttributesMap, Map<Edge<EditableNode>, AttributesMap> edgeAttributesMap, Map<Edge<EditableNode>, String> edgeCompassesMap, StepHandler stepHandler);

    protected void step(Graph g, StepHandler stepHandler) {
        if (stepHandler != null) {
            stepHandler.step(facade.graphToString(g));
        }
    }

    @Override
    public final String execute(String source, StepHandler stepHandler) {
        Graph parsedGraph;
        try {
            DotParser parser = new DotParser();
            parsedGraph = parser.parse(new StringReader(source));
        } catch (IOException ex) {
            return null;
        } catch (DotParseException ex) {
            ex.printStackTrace();
            return null;
        }
        return facade.graphToString(executeOnGraph(parsedGraph, stepHandler));
    }

    public final Graph executeOnGraph(Graph graph, StepHandler stepHandler) {
        Map<Node, AttributesMap> nodeAttributesMap = new HashMap<>();
        Map<Edge<EditableNode>, AttributesMap> edgeAttributesMap = new HashMap<>();
        Map<Edge<EditableNode>, String> edgeCompassesMap = new HashMap<>();
        Set<EditableNode> nodes = facade.decomposeGraph(graph, nodeAttributesMap, edgeAttributesMap, edgeCompassesMap);

        executeOnDecomposedGraph(nodes, nodeAttributesMap, edgeAttributesMap, edgeCompassesMap, stepHandler);
        return facade.composeGraph(nodes, nodeAttributesMap, edgeAttributesMap, edgeCompassesMap);
    }

    protected Graph composeGraph(Set<EditableNode> nodes, Map<Node, AttributesMap> nodeAttributesMap, Map<Edge<EditableNode>, AttributesMap> edgeAttributesMap, Map<Edge<EditableNode>, String> edgeCompassesMap) {
        StructuredGraphFacade f = new StructuredGraphFacade();
        return f.composeGraph(nodes, nodeAttributesMap, edgeAttributesMap, edgeCompassesMap);
    }

    protected void markEdge(Map<Edge<EditableNode>, AttributesMap> edgeAttributesMap, Edge<EditableNode> edge, String color, String label) {
        if (!edgeAttributesMap.containsKey(edge)) {
            edgeAttributesMap.put(edge, new AttributesMap());
        }
        edgeAttributesMap.get(edge).put("color", color);
        if (label != null) {
            edgeAttributesMap.get(edge).put("label", label);
            edgeAttributesMap.get(edge).put("fontcolor", color);
        }
    }

    protected void markEdge(Map<Edge<EditableNode>, AttributesMap> edgeAttributesMap, EditableNode from, EditableNode to, String color, String label) {
        markEdge(edgeAttributesMap, new Edge<>(from, to), color, label);
    }

    protected void markNode(Map<Node, AttributesMap> nodeAttributesMap, Node nodeName, String color) {
        if (!nodeAttributesMap.containsKey(nodeName)) {
            nodeAttributesMap.put(nodeName, new AttributesMap());
        }
        nodeAttributesMap.get(nodeName).put("color", color);
    }

    protected void hilightNoNode(Set<EditableNode> allNodes, Map<Node, AttributesMap> nodeAttributesMap) {
        for (Node n : allNodes) {
            if (nodeAttributesMap.containsKey(n)) {
                nodeAttributesMap.get(n).remove("color");
            }
        }
    }

    protected void hilightOneNode(Set<EditableNode> allNodes, Map<Node, AttributesMap> nodeAttributesMap, Node nodeName) {
        hilightNoNode(allNodes, nodeAttributesMap);
        markNode(nodeAttributesMap, nodeName, "red");
    }
}
