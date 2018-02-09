package com.jpexs.graphs.structure.factories;

import com.jpexs.graphs.structure.nodes.EditableEndIfNode;
import com.jpexs.graphs.structure.nodes.Node;

/**
 *
 * @author JPEXS
 */
public interface EndIfFactory {

    public EditableEndIfNode makeEndIfNode(Node decisionNode);
}
