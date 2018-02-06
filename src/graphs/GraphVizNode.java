package graphs;

import graphs.unstructured.BasicMutableNode;
import graphs.unstructured.Edge;
import graphs.unstructured.Node;

/**
 *
 * @author JPEXS
 */
public class GraphVizNode extends BasicMutableNode {

    protected GraphVizFacade a;

    protected GraphVizNode(GraphVizFacade a, String id) {
        super(id);
        this.a = a;
    }

    public static GraphVizNode create(GraphVizFacade a, String id) {
        GraphVizNode instance = new GraphVizNode(a, id);
        //a.addNode(instance);
        return instance;
    }

    @Override
    public void addNext(Node node) {
        super.addNext(node);
        a.addEdge(new Edge(this, node));
    }

    @Override
    public void addPrev(Node node) {
        super.addPrev(node);
        a.addEdge(new Edge(node, this));
    }

    public void addNextNoGraphUpdate(Node node) {
        super.addNext(node);
    }

    public void addPrevNoGraphUpdate(Node node) {
        super.addPrev(node);
    }

    @Override
    public void removeNext(Node node) {
        super.removeNext(node);
        a.removeEdge(this, node);
    }

    @Override
    public void removePrev(Node node) {
        super.removePrev(node);
        a.removeEdge(node, this);
    }
}
