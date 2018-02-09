package com.jpexs.graphs.structure.factories.operations;

import com.jpexs.graphs.structure.Edge;
import com.jpexs.graphs.structure.nodes.Node;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jindra
 */
public class CodeStructureDetectorProgressAdapter<T extends Node> implements CodeStructureDetectorProgressListener<T> {

    @Override
    public void step() {
    }

    @Override
    public T endIfDetected(T decisionNode, List<T> endBranchNodes, T afterNode) {
        return afterNode;
    }

    @Override
    public void edgeMarked(Edge edge, DetectedEdgeType edgeType) {

    }

    @Override
    public void nodeSelected(Node node) {
    }

    @Override
    public void updateDecisionLists(Map decistionLists) {
    }

    @Override
    public void noNodeSelected() {
    }

}
