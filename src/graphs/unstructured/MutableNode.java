package graphs.unstructured;

/**
 *
 * @author JPEXS
 */
public interface MutableNode extends Node {

    public void addNext(Node node);

    public void addPrev(Node node);

    public void removePrev(Node node);

    public void removeNext(Node node);
}
