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

import com.jpexs.graphs.structure.DecisionList;
import com.jpexs.graphs.structure.Edge;
import com.jpexs.graphs.structure.nodes.EditableEndIfNode;
import com.jpexs.graphs.structure.nodes.EditableNode;
import com.jpexs.graphs.structure.nodes.Node;
import com.jpexs.graphs.structure.operations.CodeStructureModifier;
import com.jpexs.graphs.structure.operations.CodeStructureModifierProgressListener;
import com.jpexs.graphs.structure.operations.DetectedEdgeType;
import guru.nidi.graphviz.model.MutableGraph;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.jpexs.graphs.structure.nodes.EditableJoinedNode;

/**
 *
 * @author JPEXS
 */
public class CodeStructureModifyOperation extends AbstractGraphOperation {

    public CodeStructureModifyOperation(String text) {
        super(text);
    }

    @Override
    public void executeOnMutableGraph(Set<EditableNode> nodes, Map<Node, AttributesBag> nodeAttributesMap, Map<Edge<EditableNode>, AttributesBag> edgeAttributesMap, Map<Edge<EditableNode>, String> edgeCompassesMap) {
        CodeStructureModifier mod = new CodeStructureModifier();
        EditableNode startNode = nodes.iterator().next();
        final EditableNode fStartNode = startNode;
        CodeStructureModifierProgressListener listener = new CodeStructureModifierProgressListener() {

            private EditableNode startNode = fStartNode;

            @Override
            public EditableNode endIfDetected(EditableNode decisionNode, List<EditableNode> endBranchNodes, EditableNode afterNode) {
                return afterNode;
            }

            @Override
            public void step() {
                CodeStructureModifyOperation.this.step(currentGraph);
                regenerate();
            }

            @Override
            public void edgeMarked(Edge<EditableNode> edge, DetectedEdgeType edgeType) {
                String color = "black";
                String label = "";
                String compass;
                String compasses[];
                String newcompass;
                boolean alreadyHasColor = edgeAttributesMap.containsKey(edge) && edgeAttributesMap.get(edge).containsKey("color");
                switch (edgeType) {
                    case BACK:
                        color = "darkorchid1";
                        if (!edgeCompassesMap.containsKey(edge)) {
                            edgeCompassesMap.put(edge, ":");
                        }
                        compass = edgeCompassesMap.get(edge);
                        compasses = compass.split(":");
                        newcompass = (edge.from.getNext().size() > 1 ? "se" : "") + ":ne";
                        if (compasses.length > 0) {
                            newcompass = compasses[0] + ":ne";
                        }
                        edgeCompassesMap.put(edge, newcompass);
                        label = "back";
                        break;
                    case GOTO:
                        color = "brown";
                        label = "goto";
                        break;
                    case OUTSIDEIF:
                        int branchIndex = edge.from.getNext().indexOf(edge.to);
                        int otherBranchIndex = branchIndex == 0 ? 1 : 0;
                        Edge<EditableNode> otherEdge = new Edge<>(edge.from, (EditableNode) edge.from.getNext().get(otherBranchIndex));
                        edgeCompassesMap.put(edge, (branchIndex == 0 ? "sw" : "se") + ":n");
                        edgeCompassesMap.put(otherEdge, "s:n");
                        if (alreadyHasColor) {
                            return;
                        }
                        color = "chocolate1";
                        label = "outside";
                        break;
                }
                CodeStructureModifyOperation.this.markEdge(edgeAttributesMap, edge, color, label);
                regenerate();
            }

            @Override
            public void nodeSelected(EditableNode node) {
                CodeStructureModifyOperation.this.hilightOneNode(nodes, nodeAttributesMap, node);
                regenerate();
            }

            @Override
            public void updateDecisionLists(Map<Edge<EditableNode>, DecisionList<EditableNode>> decistionLists) {
                CodeStructureModifyOperation.this.updateDecisionLists(currentGraph, decistionLists, edgeAttributesMap);
                regenerate();
            }

            @Override
            public void noNodeSelected() {
                CodeStructureModifyOperation.this.hilightNoNode(nodes, nodeAttributesMap);
                regenerate();
            }

            @Override
            public void endIfAdded(EditableEndIfNode endIfNode) {
                nodes.add(endIfNode);
                for (Node prev : endIfNode.getPrev()) {
                    @SuppressWarnings("unchecked")
                    EditableNode prevEditable = (EditableNode) prev;
                    edgeCompassesMap.put(new Edge<>(prevEditable, endIfNode), "s:");
                }
                for (Node next : endIfNode.getNext()) {
                    @SuppressWarnings("unchecked")
                    EditableNode nextEditable = (EditableNode) next;
                    edgeCompassesMap.put(new Edge<>(endIfNode, nextEditable), "s:");
                }
                @SuppressWarnings("unchecked")
                EditableNode ifNode = (EditableNode) endIfNode.getIfNode();
                List<Node> ifNodeNext = ifNode.getNext();
                @SuppressWarnings("unchecked")
                EditableNode onTrue = (EditableNode) ifNodeNext.get(0);
                @SuppressWarnings("unchecked")
                EditableNode onFalse = (EditableNode) ifNodeNext.get(1);
                Edge<EditableNode> onTrueStartEdge = new Edge<>(ifNode, onTrue);
                Edge<EditableNode> onFalseStartEdge = new Edge<>(ifNode, onFalse);
                edgeCompassesMap.put(onTrueStartEdge, "sw:n");
                edgeCompassesMap.put(onFalseStartEdge, "se:n");
                if (!edgeAttributesMap.containsKey(onTrueStartEdge)) {
                    edgeAttributesMap.put(onTrueStartEdge, new AttributesBag());
                }
                if (!edgeAttributesMap.containsKey(onFalseStartEdge)) {
                    edgeAttributesMap.put(onFalseStartEdge, new AttributesBag());
                }
                AttributesBag onTrueAttr = edgeAttributesMap.get(onTrueStartEdge);
                AttributesBag onFalseAttr = edgeAttributesMap.get(onFalseStartEdge);
                if (!onTrueAttr.containsKey("color")) {
                    onTrueAttr.put("color", "darkgreen");
                }
                if (!onFalseAttr.containsKey("color")) {
                    onFalseAttr.put("color", "red");
                }

                @SuppressWarnings("unchecked")
                EditableNode onTrueFinish = (EditableNode) endIfNode.getPrev().get(0);
                @SuppressWarnings("unchecked")
                EditableNode onFalseFinish = (EditableNode) endIfNode.getPrev().get(1);
                Edge<EditableNode> onTrueFinishEdge = new Edge<>(onTrueFinish, endIfNode);
                Edge<EditableNode> onFalseFinishEdge = new Edge<>(onFalseFinish, endIfNode);
                if (onTrueFinishEdge.equals(onTrueStartEdge)) {
                    edgeCompassesMap.put(onTrueFinishEdge, "sw:nw");
                } else {
                    edgeCompassesMap.put(onTrueFinishEdge, "s:nw");
                }
                if (onFalseFinishEdge.equals(onFalseStartEdge)) {
                    edgeCompassesMap.put(onFalseFinishEdge, "se:ne");
                } else {
                    edgeCompassesMap.put(onFalseFinishEdge, "s:ne");
                }
                if (!edgeAttributesMap.containsKey(onTrueFinishEdge)) {
                    edgeAttributesMap.put(onTrueFinishEdge, new AttributesBag());
                }
                if (!edgeAttributesMap.containsKey(onFalseFinishEdge)) {
                    edgeAttributesMap.put(onFalseFinishEdge, new AttributesBag());
                }
                markTrueFalseOrder(startNode, new LinkedHashSet<>(), edgeAttributesMap);
                regenerate();
            }

            private void regenerate() {
                regenerateGraph(nodes, nodeAttributesMap, edgeAttributesMap, edgeCompassesMap);
            }

            @Override
            public void nodesJoined(EditableJoinedNode node) {
                List<String> labels = new ArrayList<>();
                String shape;
                /*=null;*/
                for (Node subNode : node.getAllSubNodes()) {
                    if (subNode.getId().equals("start")) {
                        startNode = node;
                    }
                    if (nodeAttributesMap.containsKey(subNode)) {
                        AttributesBag attr = nodeAttributesMap.get(subNode);
                        if (attr.containsKey("label")) {
                            labels.add(attr.get("label").toString());
                        } else {
                            labels.add(subNode.getId());
                        }
                        /*if (attr.containsKey("shape")) {
                            String nshape = attr.get("shape").toString();
                            if (shape == null) {
                                shape = nshape;
                            }
                        } else {
                            shape = "";
                        }*/
                    } else {
                        labels.add(subNode.getId());
                    }
                    nodes.remove((EditableNode) subNode);
                }
                shape = "box";

                AttributesBag nattr = new AttributesBag();

                if (!labels.isEmpty()) {
                    nattr.put("label", String.join("\\l", labels));
                    nodeAttributesMap.put(node, nattr);
                }
                if (/*shape != null &&*/!shape.isEmpty()) {
                    nattr.put("shape", shape);
                    nodeAttributesMap.put(node, nattr);
                }
                if (node == startNode) {
                    //make startNode first again
                    List<com.jpexs.graphs.structure.nodes.EditableNode> oldCopy = new ArrayList<>(nodes);
                    nodes.clear();
                    nodes.add(startNode);
                    nodes.addAll(oldCopy);
                } else {
                    nodes.add(node);
                }
                markTrueFalseOrder(startNode, new LinkedHashSet<>(), edgeAttributesMap);
            }
        };
        mod.addListener(listener);
        markTrueFalseOrder(startNode, new LinkedHashSet<>(), edgeAttributesMap);
        mod.execute(startNode, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    private void markTrueFalseOrder(EditableNode n, Set<EditableNode> visited, Map<Edge<EditableNode>, AttributesBag> edgeAttributesMap) {
        if (visited.contains(n)) {
            return;
        }
        String branchLabels[] = new String[]{"+", "-"};
        if (n instanceof EditableEndIfNode) {
            for (int i = 0; i < n.getPrev().size(); i++) {
                Node prev = n.getPrev().get(i);
                @SuppressWarnings("unchecked")
                EditableNode prevEditable = (EditableNode) prev;
                Edge<EditableNode> edge = new Edge<>(prevEditable, n);
                if (!edgeAttributesMap.containsKey(edge)) {
                    edgeAttributesMap.put(edge, new AttributesBag());
                }
                edgeAttributesMap.get(edge).put("headlabel", branchLabels[i]);
            }
        }
        visited.add(n);
        if (n.getNext().size() == 2) { //more than 2 = some of them are gotos
            for (int i = 0; i < n.getNext().size(); i++) {
                Node next = n.getNext().get(i);
                @SuppressWarnings("unchecked")
                EditableNode nextEditable = (EditableNode) next;
                Edge<EditableNode> edge = new Edge<>(n, nextEditable);
                if (!edgeAttributesMap.containsKey(edge)) {
                    edgeAttributesMap.put(edge, new AttributesBag());
                }
                edgeAttributesMap.get(edge).put("taillabel", branchLabels[i]);
            }
        }
        for (Node next : n.getNext()) {
            @SuppressWarnings("unchecked")
            EditableNode nextEditable = (EditableNode) next;
            markTrueFalseOrder(nextEditable, visited, edgeAttributesMap);
        }
    }

    private void updateDecisionLists(MutableGraph g, Map<Edge<EditableNode>, DecisionList<EditableNode>> decistionLists, Map<Edge<EditableNode>, AttributesBag> edgeAttributesMap) {
        boolean displayDecisionLists = false;
        for (Edge<EditableNode> edge : decistionLists.keySet()) {
            if (!edgeAttributesMap.containsKey(edge)) {
                edgeAttributesMap.put(edge, new AttributesBag());
            }
            if (displayDecisionLists) {
                edgeAttributesMap.get(edge).put("label", decistionLists.get(edge).isEmpty() ? "(empty)" : decistionLists.get(edge).toString());
                edgeAttributesMap.get(edge).put("fontcolor", "red");
            }
        }
    }
}
