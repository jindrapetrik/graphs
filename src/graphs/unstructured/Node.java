package graphs.unstructured;

import java.util.List;

/**
 *
 * @author Jindra
 */
public interface Node extends Comparable<Node> {

    public List<Node> getNext();

    public List<Node> getPrev();

    public String getId();
}
