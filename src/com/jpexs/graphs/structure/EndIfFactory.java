package com.jpexs.graphs.structure;

import com.jpexs.graphs.structure.nodes.Node;
import com.jpexs.graphs.structure.nodes.EditableEndIfNode;

/**
 *
 * @author JPEXS
 */
public interface EndIfFactory {

    public EditableEndIfNode makeEndIfNode(Node decisionNode);
}
