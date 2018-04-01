package com.jpexs.graphs.graphviz.graph.operations.codestructure;

import com.jpexs.graphs.codestructure.Edge;
import com.jpexs.graphs.codestructure.nodes.EditableNode;
import com.jpexs.graphs.codestructure.nodes.Node;
import com.jpexs.graphs.graphviz.dot.parser.DotId;
import com.jpexs.graphs.graphviz.graph.AttributesMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class DecomposedGraph {

    DotId id;
    AttributesMap graphAttributes;

    Set<EditableNode> nodes;

    Map<Node, AttributesMap> nodeAttributesMap;
    Map<Edge<EditableNode>, AttributesMap> edgeAttributesMap;
    Map<Edge<EditableNode>, String> edgeCompassesMap;

    public DecomposedGraph(DotId id, AttributesMap graphAttributes, Set<EditableNode> nodes, Map<Node, AttributesMap> nodeAttributesMap, Map<Edge<EditableNode>, AttributesMap> edgeAttributesMap, Map<Edge<EditableNode>, String> edgeCompassesMap) {
        this.id = id;
        this.graphAttributes = graphAttributes;
        this.nodes = nodes;
        this.nodeAttributesMap = nodeAttributesMap;
        this.edgeAttributesMap = edgeAttributesMap;
        this.edgeCompassesMap = edgeCompassesMap;
    }

    public DotId getId() {
        return id;
    }

    public AttributesMap getGraphAttributes() {
        return graphAttributes;
    }

    public Set<EditableNode> getNodes() {
        return nodes;
    }

    public Map<Node, AttributesMap> getNodeAttributesMap() {
        return nodeAttributesMap;
    }

    public Map<Edge<EditableNode>, AttributesMap> getEdgeAttributesMap() {
        return edgeAttributesMap;
    }

    public Map<Edge<EditableNode>, String> getEdgeCompassesMap() {
        return edgeCompassesMap;
    }

}
