package com.jpexs.graphs.gui;

import com.jpexs.graphs.structure.BasicMutableNode;
import com.jpexs.graphs.structure.Edge;
import com.jpexs.graphs.structure.nodes.EditableNode;
import com.jpexs.graphs.structure.nodes.Node;
import guru.nidi.graphviz.model.Compass;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.model.MutableNodePoint;
import guru.nidi.graphviz.model.Serializer;
import guru.nidi.graphviz.parse.Parser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 *
 * @author JPEXS
 */
public class GraphVizFacade {

    public String regenerateGraphString(String text) {
        MutableGraph g = graphFromString(text);
        Map<Node, AttributesBag> nodeAttributesMap = new HashMap<>();
        Map<Edge<EditableNode>, AttributesBag> edgeAttributesMap = new HashMap<>();
        Map<Edge<EditableNode>, String> edgeCompassesMap = new HashMap<>();

        Set<EditableNode> nodes = graphToNodes(g, nodeAttributesMap, edgeAttributesMap, edgeCompassesMap);
        String ret = graphToString(generateGraph(nodes, nodeAttributesMap, edgeAttributesMap, edgeCompassesMap));
        //System.out.println(ret);
        return ret;

    }

    public MutableGraph graphFromString(String text) {
        //System.out.println("parsing:");
        //System.out.println(text);
        try {
            return Parser.read(text);
        } catch (IOException ex) {
            return null;
        }
    }

    private void populateEdges(EditableNode currentNode, Set<EditableNode> visited, Set<Edge<EditableNode>> orderedEdges) {
        if (visited.contains(currentNode)) {
            return;
        }
        visited.add(currentNode);
        for (com.jpexs.graphs.structure.nodes.Node next : currentNode.getNext()) {
            @SuppressWarnings("unchecked")
            EditableNode nextEditableNode = (EditableNode) next;
            Edge<EditableNode> e = new Edge<>(currentNode, nextEditableNode);
            //System.out.println("generateGraph adding " + e);
            orderedEdges.add(e);
            populateEdges(nextEditableNode, visited, orderedEdges);
        }
    }

    public MutableGraph generateGraph(Set<EditableNode> nodes, Map<Node, AttributesBag> nodeAttributesMap, Map<Edge<EditableNode>, AttributesBag> edgeAttributesMap, Map<Edge<EditableNode>, String> edgeCompassesMap) {
        Set<EditableNode> orderedNodes = nodes;/*new TreeSet<>(new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        orderedNodes.addAll(nodes);*/;
        MutableGraph ret = Factory.mutGraph("mygraph").setDirected(true).setLabel("mygraph");
        Set<Edge<EditableNode>> orderedEdges = new LinkedHashSet<>();//HashSet<>();//new TreeSet<>();

        //System.out.println("-----------------");
        EditableNode startNode = orderedNodes.iterator().next();
        Set<EditableNode> orderedNodes2 = new LinkedHashSet<>();
        populateEdges(startNode, orderedNodes2, orderedEdges);
        /*
        for (Node node : orderedNodes) {
            for (Node next : node.getNext()) {
                orderedEdges.add(new Edge<EditableNode>(node, next));
            }
            for (Node prev : node.getPrev()) {
                orderedEdges.add(new Edge<EditableNode>(prev, node));
            }        
        }*/
        Map<String, guru.nidi.graphviz.model.MutableNode> graphNodes = new HashMap<>();

        for (Node node : orderedNodes2) {
            //orderedEdges.contains(node) ||
            //if ((nodeAttributesMap.containsKey(node) && !nodeAttributesMap.get(node).isEmpty())) 
            if (true) {
                guru.nidi.graphviz.model.MutableNode mutNode = Factory.mutNode(node.getId());
                if (nodeAttributesMap.containsKey(node)) {
                    AttributesBag attributesToSet = nodeAttributesMap.get(node);
                    for (String attrName : attributesToSet.keySet()) {
                        mutNode.add(attrName, attributesToSet.get(attrName));
                    }
                }
                //System.out.println("generateGraph adding " + node.getId());
                ret.add(mutNode);
                graphNodes.put(node.getId(), mutNode);
            }
        }
        Set<Node> edgeNodes = new LinkedHashSet<>();
        for (Edge<EditableNode> edge : orderedEdges) {
            if (!graphNodes.containsKey(edge.from.getId())) {
                graphNodes.put(edge.from.getId(), Factory.mutNode(edge.from.getId()));
                ret.add(graphNodes.get(edge.from.getId()));
            }
            if (!graphNodes.containsKey(edge.to.getId())) {
                graphNodes.put(edge.to.getId(), Factory.mutNode(edge.to.getId()));
            }
            guru.nidi.graphviz.model.MutableNode n1 = graphNodes.get(edge.from.getId());
            guru.nidi.graphviz.model.MutableNode n2 = graphNodes.get(edge.to.getId());
            guru.nidi.graphviz.model.MutableLinkSource l1 = n1;
            guru.nidi.graphviz.model.LinkTarget l2 = n2;

            String compassArr[] = new String[]{"", ""};
            if (edgeCompassesMap.containsKey(edge)) {
                String compasses = edgeCompassesMap.get(edge);
                compassArr = compasses.split(":");
            }
            l1.addLink(l2);
            AttributesBag attributesToSet = edgeAttributesMap.get(edge);
            final String setCompassArr[] = compassArr;
            n1.links().forEach(new Consumer<Link>() {
                @Override
                public void accept(Link t) {
                    MutableNodePoint fromNp = (MutableNodePoint) t.from();
                    MutableNodePoint toNp = (MutableNodePoint) t.to();

                    if (fromNp.node() == n1 && toNp.node() == n2) {

                        fromNp.setCompass(strToCompass(setCompassArr[0]));
                        if (setCompassArr.length > 1) {
                            toNp.setCompass(strToCompass(setCompassArr[1]));
                        }

                        if (attributesToSet != null) {
                            for (String attrName : attributesToSet.keySet()) {
                                Object attrValue = attributesToSet.get(attrName);
                                t.attrs().add(attrName, attrValue);
                            }
                        }
                    }
                }
            });

            edgeNodes.add(edge.from);
            edgeNodes.add(edge.to);
        }
        /*for (Node node : orderedNodes) {
            if (!edgeNodes.contains(node) || (nodeAttributesMap.containsKey(node) && !nodeAttributesMap.get(node).isEmpty())) {
                guru.nidi.graphviz.model.MutableNode mutNode = Factory.mutNode(node.getId());
                if (nodeAttributesMap.containsKey(node)) {
                    AttributesBag attributesToSet = nodeAttributesMap.get(node);
                    for (String attrName : attributesToSet.keySet()) {
                        mutNode.add(attrName, attributesToSet.get(attrName));
                    }
                }
                System.out.println("generateGraph adding " + node.getId());
                ret.add(mutNode);
            }
        }*/
        return ret;
    }

    public String graphToString(MutableGraph g) {
        String ret = new Serializer(g).serialize();
        //System.out.println("toString called:");
        //System.out.println(ret);
        return ret;
    }

    private Compass strToCompass(String c) {
        if (c.isEmpty()) {
            return null;
        }
        switch (c) {
            case "c":
                return Compass.CENTER;
            case "e":
                return Compass.EAST;
            case "n":
                return Compass.NORTH;
            case "ne":
                return Compass.NORTH_EAST;
            case "nw":
                return Compass.NORTH_WEST;
            case "s":
                return Compass.SOUTH;
            case "se":
                return Compass.SOUTH_EAST;
            case "sw":
                return Compass.SOUTH_WEST;
            case "w":
                return Compass.WEST;
        }
        return null;
    }

    private String compassToStr(Compass c) {
        if (c == null) {
            return "";
        }
        switch (c) {
            case CENTER:
                return "c";
            case EAST:
                return "e";
            case NORTH:
                return "n";
            case NORTH_EAST:
                return "ne";
            case NORTH_WEST:
                return "nw";
            case SOUTH:
                return "s";
            case SOUTH_EAST:
                return "se";
            case SOUTH_WEST:
                return "sw";
            case WEST:
                return "w";
        }
        return "";
    }

    public Set<EditableNode> graphToNodes(MutableGraph g, Map<Node, AttributesBag> nodeAttributesMap, Map<Edge<EditableNode>, AttributesBag> edgeAttributesMap, Map<Edge<EditableNode>, String> edgeCompassesMap) {
        Set<EditableNode> orderedNodeSet = new LinkedHashSet<>();
        Map<String, EditableNode> nameToNodeMap = new HashMap<>();
        g.nodes().forEach(new Consumer<MutableNode>() {
            @Override
            public void accept(MutableNode node) {

                if (!nameToNodeMap.containsKey(node.label().toString())) {
                    nameToNodeMap.put(node.label().toString(), new BasicMutableNode(node.label().toString()));
                }
                EditableNode retNode = nameToNodeMap.get(node.label().toString());
                orderedNodeSet.add(retNode);
                //System.out.println("- adding " + retNode);

                Iterator<Map.Entry<String, Object>> nodeAttrIterator = node.attrs().iterator();
                AttributesBag nodeAttributes = new AttributesBag();
                while (nodeAttrIterator.hasNext()) {
                    Map.Entry<String, Object> entry = nodeAttrIterator.next();
                    nodeAttributes.put(entry.getKey(), entry.getValue());
                }
                nodeAttributesMap.put(retNode, nodeAttributes);

                /*String id = node.label().toString();
                if (!nameToNodeMap.containsKey(id)) {
                    Node n = new BasicMutableNode(id);
                    orderedNodeSet.add(n);
                }*/
                node.links().forEach(new Consumer<Link>() {
                    @Override
                    public void accept(Link link) {
                        MutableNodePoint fromNodePoint = (MutableNodePoint) link.from();
                        String fromCompass = compassToStr(fromNodePoint.compass());
                        String fromId = fromNodePoint.node().label().toString();
                        MutableNodePoint toNodePoint = (MutableNodePoint) link.to();
                        String toCompass = compassToStr(toNodePoint.compass());
                        String toId = toNodePoint.node().label().toString();
                        String edgeCompass = fromCompass + ":" + toCompass;

                        if (!nameToNodeMap.containsKey(fromId)) {
                            EditableNode n = new BasicMutableNode(fromId);
                            orderedNodeSet.add(n);
                            nameToNodeMap.put(fromId, n);
                        }
                        if (!nameToNodeMap.containsKey(toId)) {
                            EditableNode n = new BasicMutableNode(toId);
                            orderedNodeSet.add(n);
                            nameToNodeMap.put(toId, n);
                        }
                        EditableNode fromNode = (EditableNode) nameToNodeMap.get(fromId);
                        EditableNode toNode = (EditableNode) nameToNodeMap.get(toId);
                        fromNode.addNext(toNode);
                        toNode.addPrev(fromNode);
                        Edge<EditableNode> edge = new Edge<>(fromNode, toNode);
                        //System.out.println("graphToNodes - adding edge " + edge);

                        if (!edgeCompass.equals(":")) {
                            edgeCompassesMap.put(edge, edgeCompass);
                        }
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

        Set<EditableNode> orderedNodeSet2 = new LinkedHashSet<>();
        //System.out.println("Graph to nodes:");
        EditableNode firstNode = orderedNodeSet.iterator().next();
        //System.out.println("first node:" + firstNode);
        populateEdges(firstNode, orderedNodeSet2, new LinkedHashSet<>());
        //System.out.println("/Graph of nodes");
        //System.out.println("orderedNodeSet2.size=" + orderedNodeSet2.size());

        return orderedNodeSet2;
    }
}
