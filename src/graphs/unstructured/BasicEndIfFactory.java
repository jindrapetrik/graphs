package graphs.unstructured;

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
