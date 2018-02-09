package com.jpexs.graphs.structure;

import com.jpexs.graphs.structure.nodes.EditableEndIfNode;
import com.jpexs.graphs.structure.nodes.Node;

/**
 *
 * @author JPEXS
 */
public class BasicMutableEndIfNode extends BasicMutableNode implements EditableEndIfNode {

    private Node ifNode;
    public static String ID_PREFIX = "endif-";

    public BasicMutableEndIfNode(Node ifNode) {
        super(ID_PREFIX + ifNode.getId());
        this.ifNode = ifNode;
    }

    @Override
    public Node getIfNode() {
        return ifNode;
    }

}
