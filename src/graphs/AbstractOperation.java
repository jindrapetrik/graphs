package graphs;

import graphs.unstructured.BasicMutableNode;
import graphs.unstructured.Edge;
import graphs.unstructured.Node;
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
import java.util.function.Consumer;

/**
 *
 * @author JPEXS
 */
public abstract class AbstractOperation implements Operation {

    MutableGraph g;
    protected GraphVizFacade a;
    private String source;

    public AbstractOperation(String source) {
        this.source = source;
    }

    protected String nodesToString(String join, Collection<Node> nodes) {
        List<String> strs = new ArrayList<>();
        for (Node n : nodes) {
            strs.add(n.toString());
        }
        return String.join(join, strs);
    }

    protected abstract void executeOnMutableGraph(Map<String, Node> nodes);
    protected StepHandler stepHandler;

    @Override
    public void setStepHandler(StepHandler stepHandler) {
        this.stepHandler = stepHandler;
    }

    protected void step(MutableGraph g) {
        if (this.stepHandler != null) {
            stepHandler.step(graphToString(g));
        }
    }

    @Override
    public String execute() {
        MutableGraph g;
        try {
            g = Parser.read(source);
            a = new GraphVizFacade(g);
        } catch (IOException ex) {
            return null;
        }
        this.g = g;
        executeOnMutableGraph(a.getAllNodes());
        return graphToString(g);
    }

    protected String graphToString(MutableGraph g) {
        return new Serializer(g).serialize();
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
    protected void markEdge(MutableGraph g, Edge fromTo, String color) {
        markEdge(g, fromTo.from, fromTo.to, color);
    }

    protected void markEdge(MutableGraph g, Node from, Node to, String color) {
        MutableNode node = getMutableNode(g, from);
        node.links().forEach(new Consumer<Link>() {
            @Override
            public void accept(Link t) {
                if (((MutableNodePoint) t.to()).node().label().toString().equals(to.getId())) {
                    t.attrs().add("color", color);
                }
            }
        });
    }

    protected void markNode(MutableGraph g, Node nodeName, String color) {
        g.nodes().forEach(new Consumer<MutableNode>() {
            @Override
            public void accept(MutableNode t) {
                if (t.label().toString().equals(nodeName.getId())) {
                    t.attrs().add("color", color);
                }
            }
        });
    }

    protected void hilightNoNode(MutableGraph g) {
        g.nodes().forEach(new Consumer<MutableNode>() {
            @Override
            public void accept(MutableNode t) {
                t.attrs().add("color", "black");
            }
        });
    }

    protected void hilightOneNode(MutableGraph g, Node nodeName) {
        g.nodes().forEach(new Consumer<MutableNode>() {
            @Override
            public void accept(MutableNode t) {
                if (t.label().toString().equals(nodeName.getId())) {
                    t.attrs().add("color", "red");
                } else {
                    t.attrs().add("color", "black");
                }
            }
        });
    }
}
