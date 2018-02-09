package com.jpexs.graphs.structure.factories;

import com.jpexs.graphs.structure.nodes.EditableEndIfNode;

/**
 *
 * @author JPEXS
 */
public interface EditableEndIfFactory<T> {

    public EditableEndIfNode makeEndIfNode(T decisionNode);
}
