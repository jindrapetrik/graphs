package graphs.unstructured;

import graphs.unstructured.MutableNode;
import graphs.unstructured.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class BasicMutableNode implements MutableNode {

    private String id;
    private List<Node> nextNodes = new ArrayList<>();
    private List<Node> prevNodes = new ArrayList<>();

    public BasicMutableNode(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "\"" + id + "\"";
    }

    public void addNext(Node node) {
        nextNodes.add(node);
    }

    public void addPrev(Node node) {
        prevNodes.add(node);
    }

    @Override
    public List<Node> getNext() {
        return new ArrayList<>(nextNodes);
    }

    @Override
    public List<Node> getPrev() {
        return new ArrayList<>(prevNodes);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BasicMutableNode other = (BasicMutableNode) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public void removePrev(Node node) {
        prevNodes.remove(node);
    }

    @Override
    public void removeNext(Node node) {
        nextNodes.remove(node);
    }

    @Override
    public int compareTo(Node o) {
        return getId().compareTo(o.getId());
    }

    @Override
    public void setPrev(int index, Node node) {
        prevNodes.set(index, node);
    }

    @Override
    public void setNext(int index, Node node) {
        nextNodes.set(index, node);
    }

}
