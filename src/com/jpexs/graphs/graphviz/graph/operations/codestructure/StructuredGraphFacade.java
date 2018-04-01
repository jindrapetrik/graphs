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
import com.jpexs.graphs.graphviz.graph.NodeId;
import com.jpexs.graphs.graphviz.graph.NodeIdToAttributes;
import com.jpexs.graphs.codestructure.BasicEditableNode;
import com.jpexs.graphs.codestructure.Edge;
import com.jpexs.graphs.codestructure.nodes.EditableNode;
import com.jpexs.graphs.codestructure.nodes.JoinedNode;
import com.jpexs.graphs.codestructure.nodes.Node;
import com.jpexs.graphs.codestructure.nodes.PrefixedNode;
import com.jpexs.graphs.graphviz.dot.parser.DotId;
import com.jpexs.graphs.graphviz.graph.GraphBase;
import com.jpexs.graphs.graphviz.graph.SubGraph;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class StructuredGraphFacade {

    public static final String IGNORE_ATTRIBUTE = "_ignore";
    public static final String IGNORE_ATTRIBUTES_ATTRIBUTE = "_ignoreattrib";

    public String recompose(String text) {
        Graph g = graphFromString(text);
        List<DecomposedGraph> graphs = decomposeGraph(g);
        String ret = graphToString(composeGraph(graphs));
        return ret;

    }

    public Graph graphFromString(String text) {
        try {
            DotParser parser = new DotParser();
            Graph ret = parser.parse(new StringReader(text));
            return ret;
        } catch (IOException ex) {
            return null;
        } catch (DotParseException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void populateEdges(EditableNode currentNode, Set<EditableNode> visited, Set<Edge<EditableNode>> orderedEdges) {
        if (visited.contains(currentNode)) {
            return;
        }
        visited.add(currentNode);
        for (com.jpexs.graphs.codestructure.nodes.EditableNode next : currentNode.getNext()) {
            Edge<EditableNode> e = new Edge<>(currentNode, next);
            orderedEdges.add(e);
            populateEdges(next, visited, orderedEdges);
        }
    }

    private DotId nodeToDotId(Node n) {
        try {
            if (n instanceof JoinedNode) {
                JoinedNode jn = (JoinedNode) n;
                List<DotId> snIds = new ArrayList<>();
                for (Node sn : jn.getAllSubNodes()) {
                    snIds.add(nodeToDotId(sn));
                }
                return DotId.join(new DotId(jn.getIdDelimiter(), false), snIds);
            }
            if (n instanceof PrefixedNode) {
                PrefixedNode pn = (PrefixedNode) n;
                DotId prefix = new DotId(pn.getIdPrefix(), false);
                DotId original = nodeToDotId(pn.getOriginalNode());
                return DotId.join("", prefix, original);
            }
            return DotId.fromString(n.getId());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public Graph composeGraph(List<DecomposedGraph> gs) {
        DecomposedGraph mainGraph = gs.get(0);
        Graph ret = (Graph) composeOneGraph(false, mainGraph.getId(), mainGraph.graphAttributes, mainGraph.getNodes(), mainGraph.getNodeAttributesMap(), mainGraph.getEdgeAttributesMap(), mainGraph.getEdgeCompassesMap());

        for (int i = 1; i < gs.size(); i++) {
            DecomposedGraph g = gs.get(i);
            SubGraph nextGraph = (SubGraph) composeOneGraph(true, g.getId(), g.graphAttributes, g.getNodes(), g.getNodeAttributesMap(), g.getEdgeAttributesMap(), g.getEdgeCompassesMap());
            ret.subgraphs.add(nextGraph);
        }
        return ret;
    }

    public GraphBase composeOneGraph(boolean isSubgraph, DotId id, AttributesMap graphAttributes, Set<EditableNode> nodes, Map<Node, AttributesMap> nodeAttributesMap, Map<Edge<EditableNode>, AttributesMap> edgeAttributesMap, Map<Edge<EditableNode>, String> edgeCompassesMap) {
        GraphBase ret = isSubgraph ? new SubGraph(true) : new Graph(false, true);
        ret.id = id;
        ret.graphAttributes = graphAttributes;
        Set<EditableNode> orderedNodes = nodes;

        Set<Edge<EditableNode>> orderedEdges = new LinkedHashSet<>();//HashSet<>();//new TreeSet<>();

        EditableNode startNode = orderedNodes.isEmpty() ? null : orderedNodes.iterator().next();
        Set<EditableNode> orderedNodes2 = new LinkedHashSet<>();
        if (startNode != null) {
            populateEdges(startNode, orderedNodes2, orderedEdges);
        }

        List<NodeIdToAttributes> standaloneNodes = new ArrayList<>();
        Set<Node> processedNodes = new LinkedHashSet<>();
        for (Node node : orderedNodes2) {
            NodeId nodeId = new NodeId(nodeToDotId(node));
            if (nodeAttributesMap.containsKey(node)) {
                AttributesMap attributesToSet = nodeAttributesMap.get(node);
                standaloneNodes.add(new NodeIdToAttributes(nodeId, attributesToSet.clone()));
                processedNodes.add(node);
            }

        }
        ret.nodes = standaloneNodes;
        for (Edge<EditableNode> edge : orderedEdges) {

            NodeId fromId = new NodeId(nodeToDotId(edge.from));
            NodeId toId = new NodeId(nodeToDotId(edge.to));
            com.jpexs.graphs.graphviz.graph.Edge newEdge = new com.jpexs.graphs.graphviz.graph.Edge(true, fromId, toId);
            if (edgeAttributesMap.containsKey(edge)) {
                newEdge.attributes = edgeAttributesMap.get(edge).clone();
            }
            if (edgeCompassesMap.containsKey(edge)) {
                String compasses = edgeCompassesMap.get(edge);
                String compassArr[] = compasses.split(":");
                if (compassArr.length > 0 && !compassArr[0].isEmpty()) {
                    fromId.compassPt = compassArr[0];
                }
                if (compassArr.length > 1 && !compassArr[1].isEmpty()) {
                    toId.compassPt = compassArr[1];
                }
            }
            processedNodes.add(edge.from);
            processedNodes.add(edge.to);
            ret.edges.add(newEdge);
        }
        if (startNode != null && !processedNodes.contains(startNode)) {
            standaloneNodes.add(new NodeIdToAttributes(new NodeId(nodeToDotId(startNode)), new AttributesMap()));
        }
        return ret;
    }

    public String graphToString(Graph g) {
        String ret = g.toString();
        return ret;
    }

    public List<DecomposedGraph> decomposeGraph(Graph fullGraph) {
        List<GraphBase> allGraphs = new ArrayList<>();
        allGraphs.add(fullGraph);
        allGraphs.addAll(fullGraph.subgraphs);
        List<DecomposedGraph> ret = new ArrayList<>();

        for (GraphBase gb : allGraphs) {
            Map<Node, AttributesMap> nodeAttributesMap = new LinkedHashMap<>();
            Map<Edge<EditableNode>, AttributesMap> edgeAttributesMap = new LinkedHashMap<>();
            Map<Edge<EditableNode>, String> edgeCompassesMap = new LinkedHashMap<>();

            Set<EditableNode> orderedNodeSet = new LinkedHashSet<>();
            Map<String, EditableNode> nameToNodeMap = new LinkedHashMap<>();
            for (com.jpexs.graphs.graphviz.graph.Edge srcEdge : gb.edges) {
                NodeId fromNodeId = null;
                if (srcEdge.from instanceof NodeId) {
                    fromNodeId = (NodeId) srcEdge.from;
                }
                NodeId toNodeId = null;
                if (srcEdge.to instanceof NodeId) {
                    toNodeId = (NodeId) srcEdge.to;
                }
                if (fromNodeId != null && toNodeId != null) {
                    AttributesMap at = srcEdge.attributes.clone();
                    if (at.containsKey(IGNORE_ATTRIBUTE) && "true".equals(at.get(IGNORE_ATTRIBUTE))) {
                        continue;
                    }
                    if (at.containsKey(IGNORE_ATTRIBUTES_ATTRIBUTE) && "true".equals(at.get(IGNORE_ATTRIBUTES_ATTRIBUTE))) {
                        at.clear();
                    }

                    String fromId = fromNodeId.getId().toString();
                    String toId = toNodeId.getId().toString();

                    if (!nameToNodeMap.containsKey(fromId)) {
                        nameToNodeMap.put(fromId, new BasicEditableNode(fromId));
                    }
                    if (!nameToNodeMap.containsKey(toId)) {
                        nameToNodeMap.put(toId, new BasicEditableNode(toId));
                    }
                    EditableNode fromNode = nameToNodeMap.get(fromId);
                    EditableNode toNode = nameToNodeMap.get(toId);
                    Edge<EditableNode> targetEdge = new Edge<>(fromNode, toNode);

                    edgeAttributesMap.put(targetEdge, at);
                    String compassToSet = (fromNodeId.compassPt == null ? "" : fromNodeId.compassPt) + ":" + (toNodeId.compassPt == null ? "" : toNodeId.compassPt);
                    if (!compassToSet.equals(":")) {
                        edgeCompassesMap.put(targetEdge, compassToSet);
                    }
                    fromNode.addNext(toNode);
                    toNode.addPrev(fromNode);
                    orderedNodeSet.add(fromNode);
                    orderedNodeSet.add(toNode);
                }
            }
            //we need to add nodes with attributes after the edges for start edge (its first node) to be first
            for (NodeIdToAttributes na : gb.nodes) {
                String id = na.nodeId.getId().toString();
                AttributesMap at = na.attributes.clone();
                if (at.containsKey(IGNORE_ATTRIBUTE) && "true".equals(at.get(IGNORE_ATTRIBUTE))) {
                    continue;
                }
                if (at.containsKey(IGNORE_ATTRIBUTES_ATTRIBUTE) && "true".equals(at.get(IGNORE_ATTRIBUTES_ATTRIBUTE))) {
                    at.clear();
                }

                if (!nameToNodeMap.containsKey(id)) {
                    nameToNodeMap.put(id, new BasicEditableNode(id));
                }
                EditableNode node = nameToNodeMap.get(id);

                nodeAttributesMap.put(node, at);
                orderedNodeSet.add(node);
            }
            ret.add(new DecomposedGraph(gb.getId(), gb.graphAttributes, orderedNodeSet, nodeAttributesMap, edgeAttributesMap, edgeCompassesMap));
        }
        return ret;
    }
}
