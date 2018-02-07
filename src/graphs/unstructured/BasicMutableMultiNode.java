/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphs.unstructured;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class BasicMutableMultiNode extends BasicMutableNode implements MutableMultiNode {

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
