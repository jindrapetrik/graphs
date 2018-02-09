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
package com.jpexs.graphs.structure.operations;

import com.jpexs.graphs.structure.DecisionList;
import com.jpexs.graphs.structure.Edge;
import com.jpexs.graphs.structure.nodes.EditableNode;
import com.jpexs.graphs.structure.nodes.Node;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class CodeStructureModifier {

    public void execute(EditableNode head, List<Node> loopContinues, List<Edge<EditableNode>> gotoEdges, List<Edge<EditableNode>> backEdges, List<Edge<EditableNode>> exitIfEdges) {
        Collection<EditableNode> heads = new ArrayList<>();
        heads.add(head);
        execute(heads, loopContinues, gotoEdges, backEdges, exitIfEdges);

    }

    public void execute(Collection<? extends EditableNode> heads, List<Node> loopContinues, List<Edge<EditableNode>> gotoEdges, List<Edge<EditableNode>> backEdges, List<Edge<EditableNode>> exitIfEdges) {
        NodeJoiner nodeJoiner = new NodeJoiner();
        for (CodeStructureModifierProgressListener l : listeners) {
            nodeJoiner.addListener(l);
        }
        Collection<EditableNode> joinedHeads = nodeJoiner.joinNodes(heads);
        CodeStructureDetector<EditableNode> det = new CodeStructureDetector<>();
        final EndIfNodeInjector<EditableNode> endifInjector = new EndIfNodeInjector<>();
        for (CodeStructureModifierProgressListener l : listeners) {
            endifInjector.addListener(l);
        }
        det.addListener(new CodeStructureDetectorProgressListener<EditableNode>() {
            @Override
            public EditableNode endIfDetected(EditableNode decisionNode, List<EditableNode> endBranchNodes, EditableNode afterNode) {
                EditableNode ret = afterNode;
                for (CodeStructureModifierProgressListener l : listeners) {
                    ret = l.endIfDetected(decisionNode, endBranchNodes, afterNode);
                }
                return endifInjector.injectEndIf(decisionNode, endBranchNodes, ret);
            }

            @Override
            public void step() {
                for (CodeStructureModifierProgressListener l : listeners) {
                    l.step();
                }
            }

            @Override
            public void edgeMarked(Edge<EditableNode> edge, DetectedEdgeType edgeType) {
                for (CodeStructureModifierProgressListener l : listeners) {
                    l.edgeMarked(edge, edgeType);
                }
            }

            @Override
            public void nodeSelected(EditableNode node) {
                for (CodeStructureModifierProgressListener l : listeners) {
                    l.nodeSelected(node);
                }
            }

            @Override
            public void updateDecisionLists(Map<Edge<EditableNode>, DecisionList<EditableNode>> decistionLists) {
                for (CodeStructureModifierProgressListener l : listeners) {
                    l.updateDecisionLists(decistionLists);
                }
            }

            @Override
            public void noNodeSelected() {
                for (CodeStructureModifierProgressListener l : listeners) {
                    l.noNodeSelected();
                }
            }
        });
        det.detect(joinedHeads, loopContinues, gotoEdges, backEdges, exitIfEdges);
    }

    private List<CodeStructureModifierProgressListener> listeners = new ArrayList<>();

    public void addListener(CodeStructureModifierProgressListener listener) {
        listeners.add(listener);
    }

    public void removeListener(CodeStructureModifierProgressListener listener) {
        listeners.remove(listener);
    }
}
