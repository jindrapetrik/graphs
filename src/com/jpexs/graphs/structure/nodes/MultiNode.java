package com.jpexs.graphs.structure.nodes;

import java.util.List;

/**
 *
 * @author JPEXS
 */
public interface MultiNode extends Node {

    public void addSubNode(Node node);

    public void removeSubNode(int index);

    public int getSubNodeCount();

    public Node getSubNode(int index);

    public List<Node> getAllSubNodes();
}
