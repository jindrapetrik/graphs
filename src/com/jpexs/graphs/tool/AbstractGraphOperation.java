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

import com.jpexs.graphs.structure.Edge;
import com.jpexs.graphs.structure.nodes.EditableNode;
import com.jpexs.graphs.structure.nodes.Node;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.parse.Parser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 *
 * @author JPEXS
 */
public abstract class AbstractGraphOperation implements StringOperation {

    MutableGraph currentGraph;
    protected GraphVizFacade facade;
    private String source;
    private String currentText;

    public AbstractGraphOperation(String source) {
        this.source = source;
        this.currentText = source;
        facade = new GraphVizFacade();
    }

    protected String nodesToString(String join, Collection<Node> nodes) {
        List<String> strs = new ArrayList<>();
        for (Node n : nodes) {
            strs.add(n.toString());
        }
        return String.join(join, strs);
    }

    protected abstract void executeOnMutableGraph(Set<EditableNode> nodes, Map<Node, AttributesBag> nodeAttributesMap, Map<Edge<EditableNode>, AttributesBag> edgeAttributesMap, Map<Edge<EditableNode>, String> edgeCompassesMap);
    protected StepHandler stepHandler;

    @Override
    public void setStepHandler(StepHandler stepHandler) {
        this.stepHandler = stepHandler;
    }

    protected void step(MutableGraph g) {
        if (this.stepHandler != null) {
            stepHandler.step(facade.graphToString(g));
        }
    }

    @Override
    public String execute() {
        MutableGraph parsedGraph;
        try {
            parsedGraph = Parser.read(source);
        } catch (IOException ex) {
            return null;
        }
        this.currentGraph = parsedGraph;
        Map<Node, AttributesBag> nodeAttributesMap = new HashMap<>();
        Map<Edge<EditableNode>, AttributesBag> edgeAttributesMap = new HashMap<>();
        Map<Edge<EditableNode>, String> edgeCompassesMap = new HashMap<>();
        Set<EditableNode> nodes = facade.graphToNodes(currentGraph, nodeAttributesMap, edgeAttributesMap, edgeCompassesMap);

        executeOnMutableGraph(nodes, nodeAttributesMap, edgeAttributesMap, edgeCompassesMap);
        return facade.graphToString(facade.generateGraph(nodes, nodeAttributesMap, edgeAttributesMap, edgeCompassesMap));
    }

    protected void regenerateGraph(Set<EditableNode> nodes, Map<Node, AttributesBag> nodeAttributesMap, Map<Edge<EditableNode>, AttributesBag> edgeAttributesMap, Map<Edge<EditableNode>, String> edgeCompassesMap) {
        GraphVizFacade f = new GraphVizFacade();
        currentGraph = f.generateGraph(nodes, nodeAttributesMap, edgeAttributesMap, edgeCompassesMap);
    }

    protected MutableNode getMutableNode(MutableGraph g, Node sourceNode) {
        List<MutableNode> ret = new ArrayList<>();
        g.nodes().forEach(new Consumer<MutableNode>() {
            @Override
            public void accept(MutableNode node) {
                if (sourceNode.getId().equals(node.label().toString())) {
                    ret.add(node);
                };
            }
        });
        if (ret.isEmpty()) {
            return null;
        }
        return ret.get(0);
    }

    protected void markEdge(Map<Edge<EditableNode>, AttributesBag> edgeAttributesMap, Edge<EditableNode> edge, String color, String label) {
        if (!edgeAttributesMap.containsKey(edge)) {
            edgeAttributesMap.put(edge, new AttributesBag());
        }
        edgeAttributesMap.get(edge).put("color", color);
        if (label != null) {
            edgeAttributesMap.get(edge).put("label", label);
            edgeAttributesMap.get(edge).put("fontcolor", color);
        }
    }

    protected void markEdge(Map<Edge<EditableNode>, AttributesBag> edgeAttributesMap, EditableNode from, EditableNode to, String color, String label) {
        markEdge(edgeAttributesMap, new Edge<>(from, to), color, label);
    }

    protected void markNode(Map<Node, AttributesBag> nodeAttributesMap, Node nodeName, String color) {
        if (!nodeAttributesMap.containsKey(nodeName)) {
            nodeAttributesMap.put(nodeName, new AttributesBag());
        }
        nodeAttributesMap.get(nodeName).put("color", color);
    }

    protected void hilightNoNode(Set<EditableNode> allNodes, Map<Node, AttributesBag> nodeAttributesMap) {
        for (Node n : allNodes) {
            if (nodeAttributesMap.containsKey(n)) {
                nodeAttributesMap.get(n).remove("color");
            }
        }
    }

    protected void hilightOneNode(Set<EditableNode> allNodes, Map<Node, AttributesBag> nodeAttributesMap, Node nodeName) {
        hilightNoNode(allNodes, nodeAttributesMap);
        markNode(nodeAttributesMap, nodeName, "red");
    }
}
