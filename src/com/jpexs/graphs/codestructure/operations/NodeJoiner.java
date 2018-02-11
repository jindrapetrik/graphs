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
package com.jpexs.graphs.codestructure.operations;

import com.jpexs.graphs.codestructure.factories.BasicEditableJoinedNodeFactory;
import com.jpexs.graphs.codestructure.nodes.EditableNode;
import com.jpexs.graphs.codestructure.nodes.Node;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.jpexs.graphs.codestructure.nodes.EditableJoinedNode;
import com.jpexs.graphs.codestructure.factories.EditableJoinedNodeFactory;

/**
 *
 * @author JPEXS
 */
public class NodeJoiner {

    private EditableJoinedNodeFactory joinedNodeFactory = new BasicEditableJoinedNodeFactory();

    public void setJoinedNodeFactory(EditableJoinedNodeFactory joinedNodeFactory) {
        this.joinedNodeFactory = joinedNodeFactory;
    }

    public EditableNode joinNodes(EditableNode head) {
        Collection<EditableNode> heads = new ArrayList<>();
        heads.add(head);
        Collection<EditableNode> multiHeads = joinNodes(heads);
        return multiHeads.iterator().next();
    }

    public Collection<EditableNode> joinNodes(Collection<? extends EditableNode> heads) {
        Collection<EditableNode> ret = new ArrayList<>();
        for (EditableNode head : heads) {
            ret.add(joinNodes(head, new LinkedHashSet<>()));
        }
        return ret;
    }

    private EditableNode joinNodes(EditableNode node, Set<EditableNode> visited) {
        if (visited.contains(node)) {
            return node;
        }
        final EditableNode originalNode = node;
        EditableNode result;

        EditableNode currentNode = originalNode;
        List<EditableNode> subNodesList = new ArrayList<>();
        subNodesList.add(currentNode);
        visited.add(currentNode);

        while (currentNode.getNext().size() == 1 && currentNode.getNext().get(0).getPrev().size() == 1 && !visited.contains(currentNode.getNext().get(0))) {
            currentNode = (EditableNode) currentNode.getNext().get(0);
            visited.add(currentNode);
            subNodesList.add(currentNode);
        }

        if (subNodesList.size() > 1) {
            EditableNode lastSubNode = subNodesList.get(subNodesList.size() - 1);
            EditableNode firstSubNode = subNodesList.get(0);

            List<String> subIds = new ArrayList<>();
            for (Node sub : subNodesList) {
                subIds.add(sub.getId());
            }
            EditableJoinedNode joinedNode = joinedNodeFactory.create(subIds);
            for (Node sub : subNodesList) {
                joinedNode.addSubNode(sub);
            }
            //remove connection lastSubNode->after, add connection joinedNode->after
            for (int i = 0; i < lastSubNode.getNext().size(); i++) {
                EditableNode next = lastSubNode.getNext().get(i);
                joinedNode.addNext(next);
                if (lastSubNode instanceof EditableNode) {  //it must be - TODO - make detector use only mutable
                    EditableNode lastSubNodeMutable = (EditableNode) lastSubNode;
                    lastSubNodeMutable.removeNext(next);
                    i--; //removing from iterated nexts, must decrement to not skip anything
                }
                if (next instanceof EditableNode) { //it must be - TODO - make detector use only mutable
                    EditableNode nextMutable = (EditableNode) next;
                    for (int j = 0; j < next.getPrev().size(); j++) {
                        if (next.getPrev().get(j) == lastSubNode) {
                            nextMutable.setPrev(j, joinedNode);
                        }
                    }
                }
            }
            //remove connection before->firstNode, add connection before->joinedNode
            for (int i = 0; i < firstSubNode.getPrev().size(); i++) {
                EditableNode prev = firstSubNode.getPrev().get(i);
                joinedNode.addPrev(prev);
                EditableNode firstSubNodeMutable = (EditableNode) firstSubNode;
                firstSubNodeMutable.removePrev(prev);
                i--; //removing from iterated prevs, must decrement to not skip anything
                EditableNode prevMutable = (EditableNode) prev;
                for (int j = 0; j < prev.getNext().size(); j++) {
                    if (prev.getNext().get(j) == firstSubNode) {
                        prevMutable.setNext(j, joinedNode);
                    }
                }

            }
            fireNodesJoined(joinedNode);
            fireStep();
            result = joinedNode;
        } else {
            result = originalNode;
        }

        for (EditableNode next : result.getNext()) {
            joinNodes(next, visited);
        }

        return result;
    }

    private List<NodeJoinerProgressListener> listeners = new ArrayList<>();

    public void addListener(NodeJoinerProgressListener l) {
        listeners.add(l);
    }

    public void removeListener(NodeJoinerProgressListener l) {
        listeners.remove(l);
    }

    private void fireNodesJoined(EditableJoinedNode node) {
        for (NodeJoinerProgressListener l : listeners) {
            l.nodesJoined(node);
        }
    }

    private void fireStep() {
        for (NodeJoinerProgressListener l : listeners) {
            l.step();
        }
    }
}
