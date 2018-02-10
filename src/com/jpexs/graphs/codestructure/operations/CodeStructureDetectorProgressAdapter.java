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
package com.jpexs.graphs.codestructure.operations;

import com.jpexs.graphs.codestructure.Edge;
import com.jpexs.graphs.codestructure.nodes.Node;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 * @param <N> Node type
 */
public class CodeStructureDetectorProgressAdapter<N extends Node> implements CodeStructureDetectorProgressListener<N> {

    @Override
    public void step() {
    }

    @Override
    public N endIfDetected(N decisionNode, List<N> endBranchNodes, N afterNode) {
        return afterNode;
    }

    @Override
    public void edgeMarked(Edge<N> edge, DetectedEdgeType edgeType) {

    }

    @Override
    public void nodeSelected(N node) {
    }

    @Override
    public void updateDecisionLists(Map decistionLists) {
    }

    @Override
    public void noNodeSelected() {
    }

}
