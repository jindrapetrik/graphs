/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphs;

import graphs.unstructured.BasicMutableNode;
import graphs.unstructured.Edge;
import graphs.unstructured.Node;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jindra
 */
public class TestOperation extends AbstractOperation {

    public TestOperation(String source) {
        super(source);
    }

    @Override
    protected void executeOnMutableGraph(Map<String, Node> nodes, Map<Node, AttributesBag> nodeAttributesMap, Map<Edge, AttributesBag> edgeAttributesMap, Map<Edge, String> edgeCompassesMap) {
        Node endifNode = new BasicMutableNode("endif-if");
        Node ifNode = new BasicMutableNode("if");
        Node after = new BasicMutableNode("after");
        Node ontrue = new BasicMutableNode("ontrue");

    }

}
