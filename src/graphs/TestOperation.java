package graphs;

import graphs.unstructured.BasicMutableNode;
import graphs.unstructured.Edge;
import graphs.unstructured.Node;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class TestOperation extends AbstractOperation {

    public TestOperation(String source) {
        super(source);
    }

    @Override
    protected void executeOnMutableGraph(Set<Node> nodes, Map<Node, AttributesBag> nodeAttributesMap, Map<Edge, AttributesBag> edgeAttributesMap, Map<Edge, String> edgeCompassesMap) {
        Node endifNode = new BasicMutableNode("endif-if");
        Node ifNode = new BasicMutableNode("if");
        Node after = new BasicMutableNode("after");
        Node ontrue = new BasicMutableNode("ontrue");

    }

}
