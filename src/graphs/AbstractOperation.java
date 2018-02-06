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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.function.Consumer;

/**
 *
 * @author JPEXS
 */
public abstract class AbstractOperation implements Operation {

    MutableGraph currentGraph;
    protected GraphVizFacade facade;
    private String source;
    private String currentText;

    public AbstractOperation(String source) {
        this.source = source;
        this.currentText = source;
        facade = new GraphVizFacade();
    }

    protected String nodesToString(String join, Collection<Node> nodes) {
        List<String> strs = new ArrayList<>();
        for (Node n : nodes) {
            strs.add(n.toString());
        }
        return String.join(join, strs);
    }

    protected abstract void executeOnMutableGraph(Map<String, Node> nodes, Map<Node, AttributesBag> nodeAttributesMap, Map<Edge, AttributesBag> edgeAttributesMap, Map<Edge, String> edgeCompassesMap);
    protected StepHandler stepHandler;

    @Override
    public void setStepHandler(StepHandler stepHandler) {
        this.stepHandler = stepHandler;
    }

    protected void step(MutableGraph g) {
        if (this.stepHandler != null) {
            stepHandler.step(facade.graphToString(g));
        }
    }

    @Override
    public String execute() {
        MutableGraph parsedGraph;
        try {
            parsedGraph = Parser.read(source);
        } catch (IOException ex) {
            return null;
        }
        this.currentGraph = parsedGraph;
        Map<Node, AttributesBag> nodeAttributesMap = new HashMap<>();
        Map<Edge, AttributesBag> edgeAttributesMap = new HashMap<>();
        Map<Edge, String> edgeCompassesMap = new HashMap<>();
        Map<String, Node> nodes = facade.graphToNodes(currentGraph, nodeAttributesMap, edgeAttributesMap, edgeCompassesMap);

        executeOnMutableGraph(nodes, nodeAttributesMap, edgeAttributesMap, edgeCompassesMap);
        return facade.graphToString(facade.generateGraph(new TreeSet<>(nodes.values()), nodeAttributesMap, edgeAttributesMap, edgeCompassesMap));
    }

    protected void regenerateGraph(Set<Node> nodes, Map<Node, AttributesBag> nodeAttributesMap, Map<Edge, AttributesBag> edgeAttributesMap, Map<Edge, String> edgeCompassesMap) {
        GraphVizFacade f = new GraphVizFacade();
        currentGraph = f.generateGraph(nodes, nodeAttributesMap, edgeAttributesMap, edgeCompassesMap);
    }

    protected MutableNode getMutableNode(MutableGraph g, Node sourceNode) {
        List<MutableNode> ret = new ArrayList<>();
        g.nodes().forEach(new Consumer<MutableNode>() {
            @Override
            public void accept(MutableNode node) {
                if (sourceNode.getId().equals(node.label().toString())) {
                    ret.add(node);
                };
            }
        });
        if (ret.isEmpty()) {
            return null;
        }
        return ret.get(0);
    }

    /*protected List<Node> getAllNodes(MutableGraph g, String sourceNode) {
        List<Node> nodes = new ArrayList<>();

        g.nodes().forEach(new Consumer<MutableNode>() {
            @Override
            public void accept(MutableNode node) {
                nodes.add(node.label().toString());
            }
        });
        return nodes;
    }*/
    protected void markEdge(Map<Edge, AttributesBag> edgeAttributesMap, Edge edge, String color) {
        if (!edgeAttributesMap.containsKey(edge)) {
            edgeAttributesMap.put(edge, new AttributesBag());
        }
        edgeAttributesMap.get(edge).put("color", color);
    }

    protected void markEdge(Map<Edge, AttributesBag> edgeAttributesMap, Node from, Node to, String color) {
        markEdge(edgeAttributesMap, new Edge(from, to), color);
    }

    protected void markNode(Map<Node, AttributesBag> nodeAttributesMap, Node nodeName, String color) {
        if (!nodeAttributesMap.containsKey(nodeName)) {
            nodeAttributesMap.put(nodeName, new AttributesBag());
        }
        nodeAttributesMap.get(nodeName).put("color", color);
    }

    protected void hilightNoNode(Set<Node> allNodes, Map<Node, AttributesBag> nodeAttributesMap) {
        for (Node n : allNodes) {
            if (nodeAttributesMap.containsKey(n)) {
                nodeAttributesMap.get(n).remove("color");
            }
        }
    }

    protected void hilightOneNode(Set<Node> allNodes, Map<Node, AttributesBag> nodeAttributesMap, Node nodeName) {
        hilightNoNode(allNodes, nodeAttributesMap);
        markNode(nodeAttributesMap, nodeName, "red");
    }
}
