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

import com.jpexs.graphs.codestructure.nodes.EditableNode;
import com.jpexs.graphs.codestructure.nodes.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class BasicEditableNode implements EditableNode {

    private String id;
    private List<Node> nextNodes = new ArrayList<>();
    private List<Node> prevNodes = new ArrayList<>();

    public BasicEditableNode(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "\"" + id + "\"";
    }

    public void addNext(Node node) {
        nextNodes.add(node);
    }

    public void addPrev(Node node) {
        prevNodes.add(node);
    }

    @Override
    public List<Node> getNext() {
        return new ArrayList<>(nextNodes);
    }

    @Override
    public List<Node> getPrev() {
        return new ArrayList<>(prevNodes);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BasicEditableNode other = (BasicEditableNode) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public void removePrev(Node node) {
        prevNodes.remove(node);
    }

    @Override
    public void removeNext(Node node) {
        nextNodes.remove(node);
    }

    @Override
    public int compareTo(Node o) {
        return getId().compareTo(o.getId());
    }

    @Override
    public void setPrev(int index, Node node) {
        prevNodes.set(index, node);
    }

    @Override
    public void setNext(int index, Node node) {
        nextNodes.set(index, node);
    }

    @Override
    public void addNext(int index, Node node) {
        nextNodes.add(index, node);
    }

    @Override
    public void addPrev(int index, Node node) {
        prevNodes.add(index, node);
    }

}
