package graphs.unstructured;

import java.util.List;

/**
 *
 * @author Jindra
 */
public class BasicEndIfFactory implements EndIfFactory {

    @Override
    public BasicMutableEndIfNode makeEndIfNode(Node decisionNode) {
        return new BasicMutableEndIfNode(decisionNode);

    }

}
