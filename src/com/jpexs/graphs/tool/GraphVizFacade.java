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

import com.jpexs.graphs.graphviz.dot.parser.DotParseException;
import com.jpexs.graphs.graphviz.dot.parser.DotParser;
import com.jpexs.graphs.graphviz.graph.AttributesBag;
import com.jpexs.graphs.graphviz.graph.Graph;
import com.jpexs.graphs.graphviz.graph.NodeId;
import com.jpexs.graphs.graphviz.graph.NodeIdToAttributes;
import com.jpexs.graphs.structure.BasicEditableNode;
import com.jpexs.graphs.structure.Edge;
import com.jpexs.graphs.structure.nodes.EditableNode;
import com.jpexs.graphs.structure.nodes.Node;
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
public class GraphVizFacade {

    public String regenerateGraphString(String text) {
        Graph g = graphFromString(text);
        Map<Node, AttributesBag> nodeAttributesMap = new HashMap<>();
        Map<Edge<EditableNode>, AttributesBag> edgeAttributesMap = new HashMap<>();
        Map<Edge<EditableNode>, String> edgeCompassesMap = new HashMap<>();

        Set<EditableNode> nodes = graphToNodes(g, nodeAttributesMap, edgeAttributesMap, edgeCompassesMap);
        String ret = graphToString(nodesToGraph(nodes, nodeAttributesMap, edgeAttributesMap, edgeCompassesMap));
        //System.out.println(ret);
        return ret;

    }

    public Graph graphFromString(String text) {
        //System.out.println("parsing:");
        //System.out.println(text);
        try {
            DotParser parser = new DotParser();
            return parser.parse(new StringReader(text));
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
        for (com.jpexs.graphs.structure.nodes.Node next : currentNode.getNext()) {
            @SuppressWarnings("unchecked")
            EditableNode nextEditableNode = (EditableNode) next;
            Edge<EditableNode> e = new Edge<>(currentNode, nextEditableNode);
            //System.out.println("generateGraph adding " + e);
            orderedEdges.add(e);
            populateEdges(nextEditableNode, visited, orderedEdges);
        }
    }

    public Graph nodesToGraph(Set<EditableNode> nodes, Map<Node, AttributesBag> nodeAttributesMap, Map<Edge<EditableNode>, AttributesBag> edgeAttributesMap, Map<Edge<EditableNode>, String> edgeCompassesMap) {
        Graph ret = new Graph(false, true);
        Set<EditableNode> orderedNodes = nodes;

        Set<Edge<EditableNode>> orderedEdges = new LinkedHashSet<>();//HashSet<>();//new TreeSet<>();

        EditableNode startNode = orderedNodes.iterator().next();
        Set<EditableNode> orderedNodes2 = new LinkedHashSet<>();
        populateEdges(startNode, orderedNodes2, orderedEdges);

        List<NodeIdToAttributes> standaloneNodes = new ArrayList<>();
        for (Node node : orderedNodes2) {
            NodeId nodeId = new NodeId(node.getId());
            if (nodeAttributesMap.containsKey(node)) {
                AttributesBag attributesToSet = nodeAttributesMap.get(node);
                standaloneNodes.add(new NodeIdToAttributes(nodeId, attributesToSet.clone()));
            }

        }
        ret.nodes = standaloneNodes;
        for (Edge<EditableNode> edge : orderedEdges) {

            NodeId fromId = new NodeId(edge.from.getId());
            NodeId toId = new NodeId(edge.to.getId());
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
            ret.edges.add(newEdge);
        }
        return ret;
    }

    public String graphToString(Graph g) {
        String ret = g.toString();
        return ret;
    }

    public Set<EditableNode> graphToNodes(Graph g, Map<Node, AttributesBag> nodeAttributesMap, Map<Edge<EditableNode>, AttributesBag> edgeAttributesMap, Map<Edge<EditableNode>, String> edgeCompassesMap) {
        Set<EditableNode> orderedNodeSet = new LinkedHashSet<>();
        Map<String, EditableNode> nameToNodeMap = new LinkedHashMap<>();
        for (NodeIdToAttributes na : g.nodes) {
            String id = na.nodeId.getId();
            if (!nameToNodeMap.containsKey(id)) {
                nameToNodeMap.put(id, new BasicEditableNode(id));
            }
            Node node = nameToNodeMap.get(id);
            nodeAttributesMap.put(node, na.attributes.clone());
        }
        for (com.jpexs.graphs.graphviz.graph.Edge srcEdge : g.edges) {
            NodeId fromNodeId = null;
            if (srcEdge.from instanceof NodeId) {
                fromNodeId = (NodeId) srcEdge.from;
            }
            NodeId toNodeId = null;
            if (srcEdge.to instanceof NodeId) {
                toNodeId = (NodeId) srcEdge.to;
            }
            if (fromNodeId != null && toNodeId != null) {
                String fromId = fromNodeId.getId();
                String toId = toNodeId.getId();
                if (!nameToNodeMap.containsKey(fromId)) {
                    nameToNodeMap.put(fromId, new BasicEditableNode(fromId));
                }
                if (!nameToNodeMap.containsKey(toId)) {
                    nameToNodeMap.put(toId, new BasicEditableNode(toId));
                }
                EditableNode fromNode = nameToNodeMap.get(fromId);
                EditableNode toNode = nameToNodeMap.get(toId);
                Edge<EditableNode> targetEdge = new Edge<>(fromNode, toNode);
                edgeAttributesMap.put(targetEdge, srcEdge.attributes.clone());
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
        return orderedNodeSet;
    }
}
