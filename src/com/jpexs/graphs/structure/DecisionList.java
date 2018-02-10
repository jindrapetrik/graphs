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
package com.jpexs.graphs.structure;

import com.jpexs.graphs.structure.nodes.Node;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author JPEXS
 */
public class DecisionList<T extends Node> extends ArrayList<Decision<T>> {

    public DecisionList() {
    }

    public DecisionList(Collection<? extends Decision<T>> c) {
        super(c);
    }

    public DecisionList<T> lockForChanges() {
        return this;
    }

    /*@Override
    public boolean equals(Object o) {
        throw new RuntimeException("called equals"); //FIXME
    }*/
    public boolean ifNodesEquals(DecisionList<T> other) {
        if (other.size() != size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            if (!get(i).getIfNode().equals(other.get(i).getIfNode())) {
                return false;
            }
        }
        return true;
    }

    public boolean containsOneOfNodes(Collection<T> nodes) {
        for (Decision<T> dec : this) {
            if (nodes.contains(dec.getIfNode())) {
                return true;
            }
        }
        return false;
    }

    public boolean containsDecisionNode(Decision<T> decision) {
        return containsDecisionNode(decision.getIfNode());
    }

    public boolean containsDecisionNode(T node) {
        for (Decision<T> dec : this) {
            if (dec.getIfNode().equals(node)) {
                return true;
            }
        }
        return false;
    }
}
