package com.jpexs.graphs.structure;

import com.jpexs.graphs.structure.nodes.Node;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public interface CodeStructureDetectorProgressListener<T extends Node> {

    public void step();

    //public void endIfAdded(EndIfNode node);
    public T endIfDetected(T decisionNode, List<T> endBranchNodes, T afterNode);

    public void edgeMarked(Edge<T> edge, EdgeType edgeType);

    public void nodeSelected(T node);

    public void updateDecisionLists(Map<Edge<T>, DecisionList<T>> decistionLists);

    public void noNodeSelected();
}
