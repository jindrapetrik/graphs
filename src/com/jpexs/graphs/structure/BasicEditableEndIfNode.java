package com.jpexs.graphs.structure;

import com.jpexs.graphs.structure.nodes.EditableEndIfNode;
import com.jpexs.graphs.structure.nodes.Node;

/**
 *
 * @author JPEXS
 */
public class BasicEditableEndIfNode extends BasicEditableNode implements EditableEndIfNode {

    private Node ifNode;
    public static String ID_PREFIX = "endif-";

    public BasicEditableEndIfNode(Node ifNode) {
        super(ID_PREFIX + ifNode.getId());
        this.ifNode = ifNode;
    }

    @Override
    public Node getIfNode() {
        return ifNode;
    }

}
