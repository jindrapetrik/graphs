/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphs;

import graphs.unstructured.Edge;
import graphs.unstructured.EdgeType;
import graphs.unstructured.Node;
import graphs.unstructured.CodeStructureDetector;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNodePoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import graphs.unstructured.CodeStructureDetectorProgressListener;
import graphs.unstructured.EndIfNode;

/**
 *
 * @author Jindra
 */
public class DetectCodeStructureOperation extends AbstractOperation {

    public DetectCodeStructureOperation(String text) {
        super(text);
    }

    @Override
    public void executeOnMutableGraph(Map<String, Node> nodes) {
        CodeStructureDetector det = new CodeStructureDetector();
        det.setEndIfFactory(new GraphVizEndIfFactory(new GraphVizFacade(g)));
        det.addListener(new CodeStructureDetectorProgressListener() {
            @Override
            public void step() {
                DetectCodeStructureOperation.this.step(g);
            }

            @Override
            public void edgeMarked(Edge edge, EdgeType edgeType) {
                String color = "black";
                switch (edgeType) {
                    case BACK:
                        color = "darkgreen";
                        break;
                    case GOTO:
                        color = "red";
                        break;
                }
                DetectCodeStructureOperation.this.markEdge(g, edge, color);
            }

            @Override
            public void nodeSelected(Node node) {
                DetectCodeStructureOperation.this.hilightOneNode(g, node);
            }

            @Override
            public void updateDecisionLists(Map<Edge, List<Node>> decistionLists) {
                DetectCodeStructureOperation.this.updateDecisionLists(g, decistionLists);
            }

            @Override
            public void noNodeSelected() {
                DetectCodeStructureOperation.this.hilightNoNode(g);
            }

            @Override
            public void endIfAdded(EndIfNode node) {

            }
        });
        det.detect(nodes.get("start"), new ArrayList<>(), new ArrayList<>());
    }

    private void updateDecisionLists(MutableGraph g, Map<Edge, List<Node>> decistionLists) {
        for (Edge edge : decistionLists.keySet()) {
            Node from = edge.from;
            Node to = edge.to;
            boolean added = false;
            for (Link l : getMutableNode(g, from).links()) {
                if (((MutableNodePoint) l.to()).node().label().toString().equals(to.getId())) {
                    l.attrs().add("label", decistionLists.get(edge).isEmpty() ? "(empty)" : nodesToString(".", decistionLists.get(edge)));
                    l.attrs().add("fontcolor", "red");
                    added = true;
                }
            }
            if (!added) {
                new Exception("decision edge not found:" + edge).printStackTrace();
            }
        }
    }
}
