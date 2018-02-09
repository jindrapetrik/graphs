package com.jpexs.graphs.gui;

import com.jpexs.graphs.structure.BasicMutableNode;
import com.jpexs.graphs.structure.Edge;
import com.jpexs.graphs.structure.nodes.Node;
import java.util.Map;
import java.util.Set;
import com.jpexs.graphs.structure.nodes.EditableNode;

/**
 *
 * @author JPEXS
 */
public class TestOperation extends AbstractOperation {

    public TestOperation(String source) {
        super(source);
    }

    @Override
    protected void executeOnMutableGraph(Set<EditableNode> nodes, Map<Node, AttributesBag> nodeAttributesMap, Map<Edge<EditableNode>, AttributesBag> edgeAttributesMap, Map<Edge<EditableNode>, String> edgeCompassesMap) {
        Node endifNode = new BasicMutableNode("endif-if");
        Node ifNode = new BasicMutableNode("if");
        Node after = new BasicMutableNode("after");
        Node ontrue = new BasicMutableNode("ontrue");

    }

}
