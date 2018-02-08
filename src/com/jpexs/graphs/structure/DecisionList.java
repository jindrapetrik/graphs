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
}
