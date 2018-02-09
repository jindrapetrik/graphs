package com.jpexs.graphs.structure.factories.operations;

import com.jpexs.graphs.structure.DecisionList;
import com.jpexs.graphs.structure.Edge;
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

    public void edgeMarked(Edge<T> edge, DetectedEdgeType edgeType);

    public void nodeSelected(T node);

    public void updateDecisionLists(Map<Edge<T>, DecisionList<T>> decistionLists);

    public void noNodeSelected();
}
