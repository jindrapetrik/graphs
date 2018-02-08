package graphs;

import graphs.unstructured.CodeStructureDetector;
import graphs.unstructured.CodeStructureDetectorProgressListener;
import graphs.unstructured.DecisionList;
import graphs.unstructured.Edge;
import graphs.unstructured.EdgeType;
import graphs.unstructured.EndIfNode;
import graphs.unstructured.MultiNode;
import graphs.unstructured.Node;
import guru.nidi.graphviz.model.MutableGraph;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class DetectCodeStructureOperation extends AbstractOperation {

    public DetectCodeStructureOperation(String text) {
        super(text);
    }

    @Override
    public void executeOnMutableGraph(Set<Node> nodes, Map<Node, AttributesBag> nodeAttributesMap, Map<Edge, AttributesBag> edgeAttributesMap, Map<Edge, String> edgeCompassesMap) {
        CodeStructureDetector det = new CodeStructureDetector();
        Node startNode = nodes.iterator().next();
        final Node fStartNode = startNode;
        det.addListener(new CodeStructureDetectorProgressListener() {

            private Node startNode = fStartNode;

            @Override
            public void step() {
                DetectCodeStructureOperation.this.step(currentGraph);
                regenerate();
            }

            @Override
            public void edgeMarked(Edge edge, EdgeType edgeType) {
                String color = "black";
                String label = "";
                boolean alreadyHasColor = edgeAttributesMap.containsKey(edge) && edgeAttributesMap.get(edge).containsKey("color");
                switch (edgeType) {
                    case BACK:
                        color = "darkorchid1";
                        if (!edgeCompassesMap.containsKey(edge)) {
                            edgeCompassesMap.put(edge, ":");
                        }
                        String compass = edgeCompassesMap.get(edge);
                        String compasses[] = compass.split(":");
                        String newcompass = (edge.from.getNext().size() > 1 ? "se" : "") + ":ne";
                        if (compasses.length > 0) {
                            newcompass = compasses[0] + ":ne";
                        }
                        edgeCompassesMap.put(edge, newcompass);
                        label = "back";
                        break;
                    case GOTO:
                        color = "brown";
                        label = "goto";
                        break;
                    case OUTSIDEIF:
                        if (alreadyHasColor) {
                            return;
                        }
                        color = "red";
                        label = "outside";
                        break;
                }
                DetectCodeStructureOperation.this.markEdge(edgeAttributesMap, edge, color, label);
                regenerate();
            }

            @Override
            public void nodeSelected(Node node) {
                DetectCodeStructureOperation.this.hilightOneNode(nodes, nodeAttributesMap, node);
                regenerate();
            }

            @Override
            public void updateDecisionLists(Map<Edge, DecisionList> decistionLists) {
                DetectCodeStructureOperation.this.updateDecisionLists(currentGraph, decistionLists, edgeAttributesMap);
                regenerate();
            }

            @Override
            public void noNodeSelected() {
                DetectCodeStructureOperation.this.hilightNoNode(nodes, nodeAttributesMap);
                regenerate();
            }

            @Override
            public void endIfAdded(EndIfNode endIfNode) {
                nodes.add(endIfNode);
                for (Node prev : endIfNode.getPrev()) {
                    edgeCompassesMap.put(new Edge(prev, endIfNode), "s:");
                }
                for (Node next : endIfNode.getNext()) {
                    edgeCompassesMap.put(new Edge(endIfNode, next), "s:");
                }
                Node ifNode = endIfNode.getIfNode();
                List<Node> ifNodeNext = ifNode.getNext();
                Node onTrue = ifNodeNext.get(0);
                Node onFalse = ifNodeNext.get(1);
                Edge onTrueEdge = new Edge(ifNode, onTrue);
                Edge onFalseEdge = new Edge(ifNode, onFalse);
                edgeCompassesMap.put(onTrueEdge, "sw:n");
                edgeCompassesMap.put(onFalseEdge, "se:n");
                if (!edgeAttributesMap.containsKey(onTrueEdge)) {
                    edgeAttributesMap.put(onTrueEdge, new AttributesBag());
                }
                if (!edgeAttributesMap.containsKey(onFalseEdge)) {
                    edgeAttributesMap.put(onFalseEdge, new AttributesBag());
                }
                AttributesBag onTrueAttr = edgeAttributesMap.get(onTrueEdge);
                AttributesBag onFalseAttr = edgeAttributesMap.get(onFalseEdge);
                if (!onTrueAttr.containsKey("color")) {
                    onTrueAttr.put("color", "darkgreen");
                }
                if (!onFalseAttr.containsKey("color")) {
                    onFalseAttr.put("color", "red");
                }

                onTrue = endIfNode.getPrev().get(0);
                onFalse = endIfNode.getPrev().get(1);
                onTrueEdge = new Edge(onTrue, endIfNode);
                onFalseEdge = new Edge(onFalse, endIfNode);
                edgeCompassesMap.put(onTrueEdge, "s:nw");
                edgeCompassesMap.put(onFalseEdge, "s:ne");
                if (!edgeAttributesMap.containsKey(onTrueEdge)) {
                    edgeAttributesMap.put(onTrueEdge, new AttributesBag());
                }
                if (!edgeAttributesMap.containsKey(onFalseEdge)) {
                    edgeAttributesMap.put(onFalseEdge, new AttributesBag());
                }
                markTrueFalseOrder(startNode, new LinkedHashSet<>(), edgeAttributesMap);
                regenerate();
            }

            private void regenerate() {
                regenerateGraph(nodes, nodeAttributesMap, edgeAttributesMap, edgeCompassesMap);
            }

            @Override
            public void multiNodeJoined(MultiNode node) {
                List<String> labels = new ArrayList<>();
                String shape;
                /*=null;*/
                for (Node subNode : node.getAllSubNodes()) {
                    if (subNode.getId().equals("start")) {
                        startNode = node;
                    }
                    if (nodeAttributesMap.containsKey(subNode)) {
                        AttributesBag attr = nodeAttributesMap.get(subNode);
                        if (attr.containsKey("label")) {
                            labels.add(attr.get("label").toString());
                        } else {
                            labels.add(subNode.getId());
                        }
                        /*if (attr.containsKey("shape")) {
                            String nshape = attr.get("shape").toString();
                            if (shape == null) {
                                shape = nshape;
                            }
                        } else {
                            shape = "";
                        }*/
                    } else {
                        labels.add(subNode.getId());
                    }
                    nodes.remove(subNode);
                }
                shape = "box";

                AttributesBag nattr = new AttributesBag();

                if (!labels.isEmpty()) {
                    nattr.put("label", String.join("\\l", labels));
                    nodeAttributesMap.put(node, nattr);
                }
                if (/*shape != null &&*/!shape.isEmpty()) {
                    nattr.put("shape", shape);
                    nodeAttributesMap.put(node, nattr);
                }
                if (node == startNode) {
                    //make startNode first again
                    List<Node> oldCopy = new ArrayList<>(nodes);
                    nodes.clear();
                    nodes.add(startNode);
                    nodes.addAll(oldCopy);
                } else {
                    nodes.add(node);
                }
                markTrueFalseOrder(startNode, new LinkedHashSet<>(), edgeAttributesMap);
            }
        });

        markTrueFalseOrder(startNode, new LinkedHashSet<>(), edgeAttributesMap);
        det.detect(startNode, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    private void markTrueFalseOrder(Node n, Set<Node> visited, Map<Edge, AttributesBag> edgeAttributesMap) {
        if (visited.contains(n)) {
            return;
        }
        String branchLabels[] = new String[]{"+", "-"};
        if (n instanceof EndIfNode) {
            for (int i = 0; i < n.getPrev().size(); i++) {
                Node prev = n.getPrev().get(i);
                Edge edge = new Edge(prev, n);
                if (!edgeAttributesMap.containsKey(edge)) {
                    edgeAttributesMap.put(edge, new AttributesBag());
                }
                edgeAttributesMap.get(edge).put("headlabel", branchLabels[i]);
            }
        }
        visited.add(n);
        if (n.getNext().size() == 2) { //more than 2 = some of them are gotos
            for (int i = 0; i < n.getNext().size(); i++) {
                Node next = n.getNext().get(i);
                Edge edge = new Edge(n, next);
                if (!edgeAttributesMap.containsKey(edge)) {
                    edgeAttributesMap.put(edge, new AttributesBag());
                }
                edgeAttributesMap.get(edge).put("taillabel", branchLabels[i]);
            }
        }
        for (Node next : n.getNext()) {
            markTrueFalseOrder(next, visited, edgeAttributesMap);
        }
    }

    private void updateDecisionLists(MutableGraph g, Map<Edge, DecisionList> decistionLists, Map<Edge, AttributesBag> edgeAttributesMap) {
        boolean displayDecisionLists = false;
        for (Edge edge : decistionLists.keySet()) {
            if (!edgeAttributesMap.containsKey(edge)) {
                edgeAttributesMap.put(edge, new AttributesBag());
            }
            if (displayDecisionLists) {
                edgeAttributesMap.get(edge).put("label", decistionLists.get(edge).isEmpty() ? "(empty)" : decistionLists.get(edge).toString());
                edgeAttributesMap.get(edge).put("fontcolor", "red");
            }
        }
    }
}
