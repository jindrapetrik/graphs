package graphs.unstructured;

/**
 *
 * @author JPEXS
 */
public class Edge implements Comparable<Edge> {

    public Node from;
    public Node to;

    public Edge(Node from, Node to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public int hashCode() {
        return (this.from.getId() + ":" + this.to.getId()).hashCode();
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
        final Edge other = (Edge) obj;
        if (!this.from.getId().equals(other.from.getId())) {
            return false;
        }
        if (!this.to.getId().equals(other.to.getId())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return from.toString() + "->" + to.toString();
    }

    @Override
    public int compareTo(Edge o) {
        int ret = from.compareTo(o.from);
        if (ret != 0) {
            return ret;
        }
        return to.compareTo(o.to);
    }

}
