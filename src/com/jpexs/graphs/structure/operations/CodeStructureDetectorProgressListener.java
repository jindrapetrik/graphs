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
