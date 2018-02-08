package com.jpexs.graphs.structure;

import com.jpexs.graphs.structure.nodes.Node;

/**
 *
 * @author JPEXS
 */
public class BasicEndIfFactory implements EndIfFactory {

    @Override
    public BasicMutableEndIfNode makeEndIfNode(Node decisionNode) {
        return new BasicMutableEndIfNode(decisionNode);

    }

}
