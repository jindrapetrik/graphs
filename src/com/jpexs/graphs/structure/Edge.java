package com.jpexs.graphs.structure;

import com.jpexs.graphs.structure.nodes.Node;

/**
 *
 * @author JPEXS
 */
public class Edge<N extends Node> implements Comparable<Edge<N>> {

    public N from;
    public N to;

    public Edge(N from, N to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public int hashCode() {
        return (this.from.getId() + ":" + this.to.getId()).hashCode();
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
        final Edge other = (Edge) obj;
        if (!this.from.getId().equals(other.from.getId())) {
            return false;
        }
        if (!this.to.getId().equals(other.to.getId())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return from.toString() + "->" + to.toString();
    }

    @Override
    public int compareTo(Edge<N> o) {
        int ret = from.compareTo(o.from);
        if (ret != 0) {
            return ret;
        }
        return to.compareTo(o.to);
    }

}
