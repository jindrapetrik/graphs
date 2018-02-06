package graphs;

import graphs.unstructured.BasicMutableEndIfNode;
import graphs.unstructured.EndIfNode;
import graphs.unstructured.MutableEndIfNode;
import graphs.unstructured.Node;
import guru.nidi.graphviz.model.MutableGraph;

/**
 *
 * @author Jindra
 */
public class GraphVizEndIf extends GraphVizNode implements MutableEndIfNode {

    private Node ifNode;

    private GraphVizEndIf(GraphVizFacade a, Node ifNode) {
        super(a, BasicMutableEndIfNode.ID_PREFIX + ifNode.getId());
        this.ifNode = ifNode;
    }

    public static GraphVizEndIf create(GraphVizFacade a, Node ifNode) {
        GraphVizEndIf instance = new GraphVizEndIf(a, ifNode);
        //a.addNode(instance);
        return instance;
    }

    @Override
    public Node getIfNode() {
        return ifNode;
    }

}
