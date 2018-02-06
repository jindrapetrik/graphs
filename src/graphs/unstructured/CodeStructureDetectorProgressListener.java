package graphs.unstructured;

import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public interface CodeStructureDetectorProgressListener {

    public void step();

    public void endIfAdded(EndIfNode node);

    public void edgeMarked(Edge edge, EdgeType edgeType);

    public void nodeSelected(Node node);

    public void updateDecisionLists(Map<Edge, List<Node>> decistionLists);

    public void noNodeSelected();
}
