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

import com.jpexs.graphs.graphviz.graph.AttributesMap;
import com.jpexs.graphs.graphviz.graph.Graph;
import com.jpexs.graphs.graphviz.graph.operations.StepHandler;
import com.jpexs.graphs.codestructure.DecisionList;
import com.jpexs.graphs.codestructure.Edge;
import com.jpexs.graphs.codestructure.nodes.EditableEndIfNode;
import com.jpexs.graphs.codestructure.nodes.EditableJoinedNode;
import com.jpexs.graphs.codestructure.nodes.EditableNode;
import com.jpexs.graphs.codestructure.nodes.Node;
import com.jpexs.graphs.codestructure.operations.CodeStructureModifier;
import com.jpexs.graphs.codestructure.operations.CodeStructureModifierProgressListener;
import com.jpexs.graphs.codestructure.operations.DetectedEdgeType;
import com.jpexs.graphs.graphviz.dot.parser.DotId;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class CodeStructureModifyOperation extends BasicDecomposedGraphOperation {

    private boolean displayDecisionLists = false;

    private class Executor implements CodeStructureModifierProgressListener {

        private List<DecomposedGraph> decomposedGraphs;
        private EditableNode startNode;
        private Set<EditableNode> nodes;
        private Map<Node, AttributesMap> nodeAttributesMap;
        private Map<Edge<EditableNode>, AttributesMap> edgeAttributesMap;
        private Map<Edge<EditableNode>, String> edgeCompassesMap;
        private StructuredGraphFacade f = new StructuredGraphFacade();
        private StepHandler stepHandler;
        private Graph currentGraph;

        public Executor(List<DecomposedGraph> decomposedGraphs, StepHandler stepHandler, Set<EditableNode> nodes, Map<Node, AttributesMap> nodeAttributesMap, Map<Edge<EditableNode>, AttributesMap> edgeAttributesMap, Map<Edge<EditableNode>, String> edgeCompassesMap) {
            this.decomposedGraphs = decomposedGraphs;
            this.startNode = nodes.iterator().next();
            this.nodes = nodes;
            this.nodeAttributesMap = nodeAttributesMap;
            this.edgeAttributesMap = edgeAttributesMap;
            this.edgeCompassesMap = edgeCompassesMap;
            this.stepHandler = stepHandler;
            regenerate();
        }

        public void execute() {
            CodeStructureModifier mod = new CodeStructureModifier();
            mod.addListener(this);
            markTrueFalseOrder(startNode, new LinkedHashSet<>(), edgeAttributesMap);
            startNode = mod.execute(startNode, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }

        @Override
        public EditableNode endIfDetected(EditableNode decisionNode, List<EditableNode> endBranchNodes, EditableNode afterNode) {
            return afterNode;
        }

        @Override
        public void step() {
            CodeStructureModifyOperation.this.step(f.composeGraph(decomposedGraphs), stepHandler);
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
                    if (alreadyHasColor) {
                        return;
                    }
                    int branchIndex = edge.from.getNext().indexOf(edge.to);
                    int otherBranchIndex = branchIndex == 0 ? 1 : 0;
                    Edge<EditableNode> otherEdge = new Edge<>(edge.from, (EditableNode) edge.from.getNext().get(otherBranchIndex));
                    edgeCompassesMap.put(edge, (branchIndex == 0 ? "sw" : "se") + ":n");
                    edgeCompassesMap.put(otherEdge, "s:n");

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
            for (EditableNode prev : endIfNode.getPrev()) {
                edgeCompassesMap.put(new Edge<>(prev, endIfNode), "s:");
            }
            for (EditableNode next : endIfNode.getNext()) {
                edgeCompassesMap.put(new Edge<>(endIfNode, next), "s:");
            }
            EditableNode ifNode = endIfNode.getIfNode();
            List<? extends EditableNode> ifNodeNext = ifNode.getNext();
            EditableNode onTrue = ifNodeNext.get(0);
            EditableNode onFalse = ifNodeNext.get(1);
            Edge<EditableNode> onTrueStartEdge = new Edge<>(ifNode, onTrue);
            Edge<EditableNode> onFalseStartEdge = new Edge<>(ifNode, onFalse);
            edgeCompassesMap.put(onTrueStartEdge, "sw:n");
            edgeCompassesMap.put(onFalseStartEdge, "se:n");
            if (!edgeAttributesMap.containsKey(onTrueStartEdge)) {
                edgeAttributesMap.put(onTrueStartEdge, new AttributesMap());
            }
            if (!edgeAttributesMap.containsKey(onFalseStartEdge)) {
                edgeAttributesMap.put(onFalseStartEdge, new AttributesMap());
            }
            AttributesMap onTrueAttr = edgeAttributesMap.get(onTrueStartEdge);
            AttributesMap onFalseAttr = edgeAttributesMap.get(onFalseStartEdge);
            if (!onTrueAttr.containsKey("color")) {
                onTrueAttr.put("color", "darkgreen");
            }
            if (!onFalseAttr.containsKey("color")) {
                onFalseAttr.put("color", "red");
            }

            EditableNode onTrueFinish = endIfNode.getPrev().get(0);
            EditableNode onFalseFinish = endIfNode.getPrev().get(1);
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
                edgeAttributesMap.put(onTrueFinishEdge, new AttributesMap());
            }
            if (!edgeAttributesMap.containsKey(onFalseFinishEdge)) {
                edgeAttributesMap.put(onFalseFinishEdge, new AttributesMap());
            }
            markTrueFalseOrder(startNode, new LinkedHashSet<>(), edgeAttributesMap);
            regenerate();
        }

        private void regenerate() {
            currentGraph = composeGraph(decomposedGraphs);
        }

        @Override
        public void nodesJoined(EditableJoinedNode node) {
            List<DotId> labels = new ArrayList<>();
            DotId labelKey = new DotId("label", false);
            String shape;
            /*=null;*/
            for (Node subNode : node.getAllSubNodes()) {
                if (subNode.getId().equals(startNode.getId())) {
                    startNode = node;
                }
                if (nodeAttributesMap.containsKey(subNode)) {
                    AttributesMap attr = nodeAttributesMap.get(subNode);
                    if (attr.containsKey(labelKey)) {
                        labels.add(attr.get(labelKey));
                    } else {
                        labels.add(DotId.fromString(subNode.getId()));
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
                    labels.add(DotId.fromString(subNode.getId()));
                }
                nodes.remove((EditableNode) subNode);
            }
            shape = "box";

            AttributesMap nattr = new AttributesMap();

            if (!labels.isEmpty()) {
                nattr.put(new DotId("label", false), DotId.join("\\l", labels));
                nodeAttributesMap.put(node, nattr);
            }
            if (/*shape != null &&*/!shape.isEmpty()) {
                nattr.put("shape", shape);
                nodeAttributesMap.put(node, nattr);
            }
            if (node == startNode) {
                //make startNode first again
                List<com.jpexs.graphs.codestructure.nodes.EditableNode> oldCopy = new ArrayList<>(nodes);
                nodes.clear();
                nodes.add(startNode);
                nodes.addAll(oldCopy);
            } else {
                nodes.add(node);
            }
            markTrueFalseOrder(startNode, new LinkedHashSet<>(), edgeAttributesMap);
        }
    }

    @Override
    protected void executeOnDecomposedGraph(List<DecomposedGraph> decomposedGraphs, StepHandler stepHandler) {
        for (DecomposedGraph dg : decomposedGraphs) {
            Executor ex = new Executor(decomposedGraphs, stepHandler, dg.nodes, dg.nodeAttributesMap, dg.edgeAttributesMap, dg.edgeCompassesMap);
            ex.execute();
        }
    }

    private void markTrueFalseOrder(EditableNode n, Set<EditableNode> visited, Map<Edge<EditableNode>, AttributesMap> edgeAttributesMap) {
        if (visited.contains(n)) {
            return;
        }
        String branchLabels[] = new String[]{"+", "-"};
        if (n instanceof EditableEndIfNode) {
            for (int i = 0; i < n.getPrev().size(); i++) {
                EditableNode prev = n.getPrev().get(i);
                Edge<EditableNode> edge = new Edge<>(prev, n);
                if (!edgeAttributesMap.containsKey(edge)) {
                    edgeAttributesMap.put(edge, new AttributesMap());
                }
                edgeAttributesMap.get(edge).put("headlabel", branchLabels[i]);
            }
        }
        visited.add(n);
        if (n.getNext().size() == 2) { //more than 2 = some of them are gotos
            for (int i = 0; i < n.getNext().size(); i++) {
                EditableNode next = n.getNext().get(i);
                Edge<EditableNode> edge = new Edge<>(n, next);
                if (!edgeAttributesMap.containsKey(edge)) {
                    edgeAttributesMap.put(edge, new AttributesMap());
                }
                edgeAttributesMap.get(edge).put("taillabel", branchLabels[i]);
            }
        }
        for (EditableNode next : n.getNext()) {
            markTrueFalseOrder(next, visited, edgeAttributesMap);
        }
    }

    private void updateDecisionLists(Graph g, Map<Edge<EditableNode>, DecisionList<EditableNode>> decistionLists, Map<Edge<EditableNode>, AttributesMap> edgeAttributesMap) {
        for (Edge<EditableNode> edge : decistionLists.keySet()) {
            if (!edgeAttributesMap.containsKey(edge)) {
                edgeAttributesMap.put(edge, new AttributesMap());
            }
            if (displayDecisionLists) {
                edgeAttributesMap.get(edge).put("label", decistionLists.get(edge).isEmpty() ? "(empty)" : decistionLists.get(edge).toString());
                edgeAttributesMap.get(edge).put("fontcolor", "red");
            }
        }
    }
}
