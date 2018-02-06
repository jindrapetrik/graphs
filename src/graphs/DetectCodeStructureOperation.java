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
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Serializer;

/**
 *
 * @author Jindra
 */
public class DetectCodeStructureOperation extends AbstractOperation {

    public DetectCodeStructureOperation(String text) {
        super(text);
    }

    @Override
    public void executeOnMutableGraph(Map<String, Node> nodes, Map<Node, AttributesBag> nodeAttributesMap, Map<Edge, AttributesBag> edgeAttributesMap) {
        CodeStructureDetector det = new CodeStructureDetector();
        det.addListener(new CodeStructureDetectorProgressListener() {
            @Override
            public void step() {
                DetectCodeStructureOperation.this.step(currentGraph);
                regenerate();
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
                DetectCodeStructureOperation.this.markEdge(edgeAttributesMap, edge, color);
                regenerate();
            }

            @Override
            public void nodeSelected(Node node) {
                DetectCodeStructureOperation.this.hilightOneNode(new HashSet<>(nodes.values()), nodeAttributesMap, node);
                regenerate();
            }

            @Override
            public void updateDecisionLists(Map<Edge, List<Node>> decistionLists) {
                DetectCodeStructureOperation.this.updateDecisionLists(currentGraph, decistionLists, edgeAttributesMap);
                regenerate();
            }

            @Override
            public void noNodeSelected() {
                DetectCodeStructureOperation.this.hilightNoNode(new HashSet<>(nodes.values()), nodeAttributesMap);
                regenerate();
            }

            @Override
            public void endIfAdded(EndIfNode node) {
                nodes.put(node.getId(), node);
                regenerate();
            }

            private void regenerate() {
                regenerateGraph(new HashSet<>(nodes.values()), nodeAttributesMap, edgeAttributesMap);
            }
        });
        //regenerateGraph(new TreeSet<>(nodes.values()), nodeAttributesMap, edgeAttributesMap);
        det.detect(nodes.get("start"), new ArrayList<>(), new ArrayList<>());
    }

    private void updateDecisionLists(MutableGraph g, Map<Edge, List<Node>> decistionLists, Map<Edge, AttributesBag> edgeAttributesMap) {
        for (Edge edge : decistionLists.keySet()) {
            if (!edgeAttributesMap.containsKey(edge)) {
                edgeAttributesMap.put(edge, new AttributesBag());
            }
            edgeAttributesMap.get(edge).put("label", decistionLists.get(edge).isEmpty() ? "(empty)" : nodesToString(".", decistionLists.get(edge)));
            edgeAttributesMap.get(edge).put("fontcolor", "red");
        }
    }
}
