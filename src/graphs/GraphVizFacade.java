/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphs;

import graphs.unstructured.BasicMutableNode;
import graphs.unstructured.Edge;
import graphs.unstructured.Node;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.model.MutableNodePoint;

import guru.nidi.graphviz.model.Serializer;
import guru.nidi.graphviz.parse.Parser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

/**
 *
 * @author Jindra
 */
public class GraphVizFacade {

    public String regenerateGraphString(String text) {
        MutableGraph g = graphFromString(text);
        Map<Node, AttributesBag> nodeAttributesMap = new HashMap<>();
        Map<Edge, AttributesBag> edgeAttributesMap = new HashMap<>();

        Map<String, Node> nodes = graphToNodes(g, nodeAttributesMap, edgeAttributesMap);
        return graphToString(generateGraph(new HashSet<>(nodes.values()), nodeAttributesMap, edgeAttributesMap));

    }

    public MutableGraph graphFromString(String text) {
        try {
            return Parser.read(text);
        } catch (IOException ex) {
            return null;
        }
    }

    public MutableGraph generateGraph(Set<Node> nodes, Map<Node, AttributesBag> nodeAttributesMap, Map<Edge, AttributesBag> edgeAttributesMap) {
        Set<Node> orderedNodes = new TreeSet<>(new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        orderedNodes.addAll(nodes);
        MutableGraph ret = Factory.mutGraph("mygraph").setDirected(true).setLabel("mygraph");
        Set<Edge> edges = new HashSet<>();
        for (Node node : orderedNodes) {
            for (Node prev : node.getPrev()) {
                edges.add(new Edge(prev, node));
            }
            for (Node next : node.getNext()) {
                edges.add(new Edge(node, next));
            }
        }
        Map<String, guru.nidi.graphviz.model.MutableNode> graphNodes = new HashMap<>();
        Set<Node> edgeNodes = new HashSet<>();
        for (Edge edge : edges) {
            if (!graphNodes.containsKey(edge.from.getId())) {
                graphNodes.put(edge.from.getId(), Factory.mutNode(edge.from.getId()));
                ret.add(graphNodes.get(edge.from.getId()));
            }
            if (!graphNodes.containsKey(edge.to.getId())) {
                graphNodes.put(edge.to.getId(), Factory.mutNode(edge.to.getId()));
                //ret.add(graphNodes.get(edge.to.getId()));
            }
            guru.nidi.graphviz.model.MutableNode n1 = graphNodes.get(edge.from.getId());
            guru.nidi.graphviz.model.MutableNode n2 = graphNodes.get(edge.to.getId());
            n1.addLink(n2);
            if (edgeAttributesMap.containsKey(edge)) {
                AttributesBag attributesToSet = edgeAttributesMap.get(edge);
                n1.links().forEach(new Consumer<Link>() {
                    @Override
                    public void accept(Link t) {
                        if (((MutableNodePoint) t.from()).node() == n1 && ((MutableNodePoint) t.to()).node() == n2) {
                            for (String attrName : attributesToSet.keySet()) {
                                Object attrValue = attributesToSet.get(attrName);
                                t.attrs().add(attrName, attrValue);
                            }

                        }
                    }
                });
            }
            edgeNodes.add(edge.from);
            edgeNodes.add(edge.to);
        }
        for (Node node : orderedNodes) {
            if (!edgeNodes.contains(node) || (nodeAttributesMap.containsKey(node) && !nodeAttributesMap.get(node).isEmpty())) {
                guru.nidi.graphviz.model.MutableNode mutNode = Factory.mutNode(node.getId());
                if (nodeAttributesMap.containsKey(node)) {
                    AttributesBag attributesToSet = nodeAttributesMap.get(node);
                    for (String attrName : attributesToSet.keySet()) {
                        mutNode.add(attrName, attributesToSet.get(attrName));
                    }
                }
                ret.add(mutNode);
            }
        }
        return ret;
    }

    public String graphToString(MutableGraph g) {
        return new Serializer(g).serialize();
    }

    public Map<String, Node> graphToNodes(MutableGraph g, Map<Node, AttributesBag> nodeAttributesMap, Map<Edge, AttributesBag> edgeAttributesMap) {
        Map<String, Node> ret = new HashMap<>();
        g.nodes().forEach(new Consumer<MutableNode>() {
            @Override
            public void accept(MutableNode node) {
                if (!ret.containsKey(node.label().toString())) {
                    ret.put(node.label().toString(), new BasicMutableNode(node.label().toString()));
                }
                Node retNode = ret.get(node.label().toString());
                Iterator<Map.Entry<String, Object>> nodeAttrIterator = node.attrs().iterator();
                AttributesBag nodeAttributes = new AttributesBag();
                while (nodeAttrIterator.hasNext()) {
                    Map.Entry<String, Object> entry = nodeAttrIterator.next();
                    nodeAttributes.put(entry.getKey(), entry.getValue());
                }
                nodeAttributesMap.put(retNode, nodeAttributes);

                String id = node.label().toString();
                if (!ret.containsKey(id)) {
                    Node n = new BasicMutableNode(id);
                    ret.put(id, n);
                }
                node.links().forEach(new Consumer<Link>() {
                    @Override
                    public void accept(Link link) {
                        String fromId = ((MutableNodePoint) link.from()).node().label().toString();
                        String toId = ((MutableNodePoint) link.to()).node().label().toString();
                        if (!ret.containsKey(fromId)) {
                            ret.put(fromId, new BasicMutableNode(fromId));
                        }
                        if (!ret.containsKey(toId)) {
                            ret.put(toId, new BasicMutableNode(toId));
                        }
                        graphs.unstructured.MutableNode fromNode = (graphs.unstructured.MutableNode) ret.get(fromId);
                        graphs.unstructured.MutableNode toNode = (graphs.unstructured.MutableNode) ret.get(toId);
                        fromNode.addNext(toNode);
                        toNode.addPrev(fromNode);
                        Edge edge = new Edge(fromNode, toNode);
                        AttributesBag edgeAttributes = new AttributesBag();
                        Iterator<Map.Entry<String, Object>> linkAttrIterator = link.attrs().iterator();
                        while (linkAttrIterator.hasNext()) {
                            Map.Entry<String, Object> entry = linkAttrIterator.next();
                            edgeAttributes.put(entry.getKey(), entry.getValue());
                        }
                        edgeAttributesMap.put(edge, edgeAttributes);

                    }
                });

            }
        });

        return ret;
    }
}
