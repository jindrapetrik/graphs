/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphs.unstructured;

import java.util.List;

/**
 *
 * @author JPEXS
 */
public interface MultiNode extends Node {

    public void addSubNode(Node node);

    public void removeSubNode(int index);

    public int getSubNodeCount();

    public Node getSubNode(int index);

    public List<Node> getAllSubNodes();
}
