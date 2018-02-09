package com.jpexs.graphs.structure.factories;

import com.jpexs.graphs.structure.BasicEditableEndIfNode;
import com.jpexs.graphs.structure.factories.EndIfFactory;
import com.jpexs.graphs.structure.nodes.Node;

/**
 *
 * @author JPEXS
 */
public class BasicEndIfFactory implements EndIfFactory {

    @Override
    public BasicEditableEndIfNode makeEndIfNode(Node decisionNode) {
        return new BasicEditableEndIfNode(decisionNode);

    }

}
