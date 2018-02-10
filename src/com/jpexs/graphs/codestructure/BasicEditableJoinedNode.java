/*
 * Copyright (C) 2018 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.graphs.codestructure;

import com.jpexs.graphs.codestructure.nodes.Node;
import java.util.ArrayList;
import java.util.List;
import com.jpexs.graphs.codestructure.nodes.EditableJoinedNode;

/**
 *
 * @author JPEXS
 */
public class BasicEditableJoinedNode extends BasicEditableNode implements EditableJoinedNode {

    private List<Node> subNodes = new ArrayList<>();

    public BasicEditableJoinedNode(String id) {
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
