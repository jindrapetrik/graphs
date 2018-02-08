package graphs.unstructured;

import graphs.unstructured.nodes.Node;
import graphs.unstructured.nodes.MutableEndIfNode;

/**
 *
 * @author JPEXS
 */
public interface EndIfFactory {

    public MutableEndIfNode makeEndIfNode(Node decisionNode);
}
