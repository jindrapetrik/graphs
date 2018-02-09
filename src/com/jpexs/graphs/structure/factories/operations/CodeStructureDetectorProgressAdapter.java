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
