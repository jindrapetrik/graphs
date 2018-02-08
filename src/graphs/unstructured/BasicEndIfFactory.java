package graphs.unstructured;

import graphs.unstructured.nodes.Node;

/**
 *
 * @author JPEXS
 */
public class BasicEndIfFactory implements EndIfFactory {

    @Override
    public BasicMutableEndIfNode makeEndIfNode(Node decisionNode) {
        return new BasicMutableEndIfNode(decisionNode);

    }

}
