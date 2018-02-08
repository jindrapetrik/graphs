package com.jpexs.graphs.structure;

import com.jpexs.graphs.structure.nodes.Node;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class Decision<T extends Node> {

    private T ifNode;
    private int branchNum;

    public Decision(T ifNode, int branchNum) {
        this.ifNode = ifNode;
        this.branchNum = branchNum;
    }

    public int getBranchNum() {
        return branchNum;
    }

    public T getIfNode() {
        return ifNode;
    }

    @Override
    public String toString() {
        return ifNode.toString() + "|" + branchNum;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.ifNode);
        hash = 47 * hash + this.branchNum;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Decision other = (Decision) obj;
        if (!Objects.equals(this.ifNode, other.ifNode)) {
            return false;
        }
        if (this.branchNum != other.branchNum) {
            return false;
        }
        return true;
    }

}
