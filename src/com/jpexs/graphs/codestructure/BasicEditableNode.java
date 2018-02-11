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
    private List<EditableNode> nextNodes = new ArrayList<>();
    private List<EditableNode> prevNodes = new ArrayList<>();

    public BasicEditableNode(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "\"" + id + "\"";
    }

    public void addNext(EditableNode node) {
        nextNodes.add(node);
    }

    public void addPrev(EditableNode node) {
        prevNodes.add(node);
    }

    @Override
    public List<? extends EditableNode> getNext() {
        return new ArrayList<>(nextNodes);
    }

    @Override
    public List<? extends EditableNode> getPrev() {
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
    public void removePrev(EditableNode node) {
        prevNodes.remove(node);
    }

    @Override
    public void removeNext(EditableNode node) {
        nextNodes.remove(node);
    }

    @Override
    public int compareTo(Node o) {
        return getId().compareTo(o.getId());
    }

    @Override
    public void setPrev(int index, EditableNode node) {
        prevNodes.set(index, node);
    }

    @Override
    public void setNext(int index, EditableNode node) {
        nextNodes.set(index, node);
    }

    @Override
    public void addNext(int index, EditableNode node) {
        nextNodes.add(index, node);
    }

    @Override
    public void addPrev(int index, EditableNode node) {
        prevNodes.add(index, node);
    }

}
