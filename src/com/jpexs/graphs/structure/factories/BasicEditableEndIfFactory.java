package com.jpexs.graphs.structure.factories;

import com.jpexs.graphs.structure.BasicEditableEndIfNode;
import com.jpexs.graphs.structure.nodes.EditableNode;

/**
 *
 * @author JPEXS
 */
public class BasicEditableEndIfFactory implements EditableEndIfFactory<EditableNode> {

    @Override
    public BasicEditableEndIfNode makeEndIfNode(EditableNode decisionNode) {
        return new BasicEditableEndIfNode(decisionNode);

    }

}
