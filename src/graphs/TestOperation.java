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
    protected void executeOnMutableGraph(Map<String, Node> nodes) {
        Node endifNode = new BasicMutableNode("endif-if");
        Node ifNode = new BasicMutableNode("if");
        Node after = new BasicMutableNode("after");
        Node ontrue = new BasicMutableNode("ontrue");

        System.err.println(graphToString(g));
        a.addEdge(new Edge(endifNode, after));
        System.err.println(graphToString(g));
        a.removeEdge(new Edge(ifNode, after));
        /*System.err.println(graphToString(g));
        a.removeEdge(new Edge(ontrue, after));
        System.err.println(graphToString(g));
        a.addEdge(new Edge(ifNode, endifNode));
        System.err.println(graphToString(g));
        a.addEdge(new Edge(ontrue, endifNode));
         */
        //addding edge endif-if->after
/*removing edge if->after
removing edge ontrue->after
addding edge if->endif-if
addding edge ontrue->endif-if*/
 /**/
        System.err.println(graphToString(g));
    }

}
