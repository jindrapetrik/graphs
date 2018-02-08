package graphs.unstructured;

import graphs.unstructured.nodes.MultiNode;
import graphs.unstructured.nodes.EndIfNode;
import graphs.unstructured.nodes.Node;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public interface CodeStructureDetectorProgressListener {

    public void step();

    public void multiNodeJoined(MultiNode node);

    public void endIfAdded(EndIfNode node);

    public void edgeMarked(Edge edge, EdgeType edgeType);

    public void nodeSelected(Node node);

    public void updateDecisionLists(Map<Edge, DecisionList> decistionLists);

    public void noNodeSelected();
}
