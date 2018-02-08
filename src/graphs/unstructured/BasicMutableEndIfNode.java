package graphs.unstructured;

/**
 *
 * @author JPEXS
 */
public class BasicMutableEndIfNode extends BasicMutableNode implements MutableEndIfNode {

    private Node ifNode;
    public static String ID_PREFIX = "endif-";

    public BasicMutableEndIfNode(Node ifNode) {
        super(ID_PREFIX + ifNode.getId());
        this.ifNode = ifNode;
    }

    @Override
    public Node getIfNode() {
        return ifNode;
    }

}
