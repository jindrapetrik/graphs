package graphs;

import graphs.unstructured.BasicMutableEndIfNode;
import graphs.unstructured.EndIfFactory;
import graphs.unstructured.EndIfNode;
import graphs.unstructured.MutableEndIfNode;
import graphs.unstructured.MutableNode;
import graphs.unstructured.Node;
import guru.nidi.graphviz.model.MutableGraph;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jindra
 */
public class GraphVizEndIfFactory implements EndIfFactory {

    private GraphVizFacade a;

    public GraphVizEndIfFactory(GraphVizFacade g) {
        this.a = g;
    }

    @Override
    public GraphVizEndIf makeEndIfNode(Node decisionNode) {
        return GraphVizEndIf.create(a, decisionNode);
    }

}
