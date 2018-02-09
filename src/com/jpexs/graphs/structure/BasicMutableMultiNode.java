package com.jpexs.graphs.structure;

import com.jpexs.graphs.structure.nodes.Node;
import java.util.ArrayList;
import java.util.List;
import com.jpexs.graphs.structure.nodes.EditableMultiNode;

/**
 *
 * @author JPEXS
 */
public class BasicMutableMultiNode extends BasicMutableNode implements EditableMultiNode {

    private List<Node> subNodes = new ArrayList<>();

    public BasicMutableMultiNode(String id) {
        super(id);
    }

    @Override
    public void addSubNode(Node node) {
        subNodes.add(node);
    }

    @Override
    public void removeSubNode(int index) {
        subNodes.remove(index);
    }

    @Override
    public int getSubNodeCount() {
        return subNodes.size();
    }

    @Override
    public Node getSubNode(int index) {
        return subNodes.get(index);
    }

    @Override
    public List<Node> getAllSubNodes() {
        return new ArrayList<>(subNodes);
    }

}
