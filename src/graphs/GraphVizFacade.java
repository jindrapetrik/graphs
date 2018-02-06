/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphs;

import graphs.unstructured.Edge;
import graphs.unstructured.Node;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.model.MutableNodePoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 *
 * @author Jindra
 */
public class GraphVizFacade {

    private MutableGraph g;

    public GraphVizFacade(MutableGraph g) {
        this.g = g;
    }

    public void addEdge(Edge edge) {
        if (edgeExists(edge)) {
            return;
        }
        System.err.println("addding edge " + edge);
        //guru.nidi.graphviz.model.Node n;
        MutableNode n = findNode(edge.from);
        if (n == null) {
            n = Factory.mutNode(edge.from.getId());
        }
        g.add(n.addLink(g)); //n.link(edge.to.getId()));
    }

    public void removeEdge(Edge edge) {
        removeEdge(edge.from, edge.to);
    }

    private MutableNode findNode(Node n) {
        List<MutableNode> exist = new ArrayList<>();
        g.nodes().forEach(new Consumer<MutableNode>() {
            @Override
            public void accept(MutableNode t) {
                if (t.label().toString().equals(n.getId())) {
                    exist.add(t);
                }
            }
        });
        return exist.isEmpty() ? null : exist.get(0);
    }

    private boolean edgeExists(Edge edge) {
        List<Boolean> exist = new ArrayList<>();
        g.nodes().forEach(new Consumer<MutableNode>() {
            @Override
            public void accept(MutableNode t) {
                List<Link> linksToRemove = new ArrayList<>();
                t.links().forEach(new Consumer<Link>() {
                    @Override
                    public void accept(Link link) {
                        if ((((MutableNodePoint) link.from()).node().label().toString().equals(edge.from.getId()))
                                && (((MutableNodePoint) link.to()).node().label().toString().equals(edge.to.getId()))) {
                            exist.add(true);
                        }
                    }
                });
            }
        });
        return !exist.isEmpty();
    }

    public void removeEdge(Node from, Node to) {
        List<Integer> numRemoved = new ArrayList<>();
        System.err.println("removing edge " + from + "->" + to);
        g.nodes().forEach(new Consumer<MutableNode>() {
            @Override
            public void accept(MutableNode t) {
                List<Link> linksToRemove = new ArrayList<>();
                t.links().forEach(new Consumer<Link>() {
                    @Override
                    public void accept(Link link) {
                        if ((((MutableNodePoint) link.from()).node().label().toString().equals(from.getId()))
                                && (((MutableNodePoint) link.to()).node().label().toString().equals(to.getId()))) {
                            linksToRemove.add(link);
                        }
                    }
                });
                if (!linksToRemove.isEmpty()) {
                    numRemoved.add(1);

                }
                for (Link l : linksToRemove) {
                    t.removeLink(l);
                }
            }
        });
        if (numRemoved.isEmpty()) {
            System.err.println("WARNING: no edge removed");
        }
    }

    public Map<String, Node> getAllNodes() {
        Map<String, Node> ret = new HashMap<>();
        g.nodes().forEach(new Consumer<MutableNode>() {
            @Override
            public void accept(MutableNode node) {
                String id = node.label().toString();
                if (!ret.containsKey(id)) {
                    Node n = GraphVizNode.create(GraphVizFacade.this, id);
                    ret.put(id, n);
                }
                node.links().forEach(new Consumer<Link>() {
                    @Override
                    public void accept(Link link) {
                        String fromId = ((MutableNodePoint) link.from()).node().label().toString();
                        String toId = ((MutableNodePoint) link.to()).node().label().toString();
                        if (!ret.containsKey(fromId)) {
                            ret.put(fromId, GraphVizNode.create(GraphVizFacade.this, fromId));
                        }
                        if (!ret.containsKey(toId)) {
                            ret.put(toId, GraphVizNode.create(GraphVizFacade.this, toId));
                        }
                        graphs.unstructured.MutableNode fromNode = (graphs.unstructured.MutableNode) ret.get(fromId);
                        graphs.unstructured.MutableNode toNode = (graphs.unstructured.MutableNode) ret.get(toId);
                        ((GraphVizNode) fromNode).addNextNoGraphUpdate(toNode);
                        ((GraphVizNode) toNode).addPrevNoGraphUpdate(fromNode);
                    }
                });

            }
        });

        return ret;
    }
}
