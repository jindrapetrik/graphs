package graphs.unstructured;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author JPEXS
 */
public class DecisionList extends ArrayList<Decision> {

    public DecisionList() {
    }

    public DecisionList(Collection<? extends Decision> c) {
        super(c);
    }

    public static DecisionList unmodifiableList(DecisionList dl) {
        ///todo
        return dl;
    }

    /*@Override
    public boolean equals(Object o) {
        throw new RuntimeException("called equals"); //FIXME
    }*/
    public boolean ifNodesEquals(DecisionList other) {
        if (other.size() != size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            if (!get(i).getIfNode().equals(other.get(i).getIfNode())) {
                return false;
            }
        }
        return true;
    }
}
