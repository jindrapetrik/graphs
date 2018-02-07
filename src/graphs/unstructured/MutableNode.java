package graphs.unstructured;

/**
 *
 * @author JPEXS
 */
public interface MutableNode extends Node {

    public void addNext(Node node);

    public void addNext(int index, Node node);

    public void addPrev(Node node);

    public void addPrev(int index, Node node);

    public void removePrev(Node node);

    public void removeNext(Node node);

    public void setPrev(int index, Node node);

    public void setNext(int index, Node node);
}
