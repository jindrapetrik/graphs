package com.jpexs.graphs.structure;

import com.jpexs.graphs.structure.nodes.Node;
import com.jpexs.graphs.structure.nodes.MutableEndIfNode;

/**
 *
 * @author JPEXS
 */
public interface EndIfFactory {

    public MutableEndIfNode makeEndIfNode(Node decisionNode);
}
