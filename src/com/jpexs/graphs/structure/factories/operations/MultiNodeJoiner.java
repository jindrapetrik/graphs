package com.jpexs.graphs.structure.factories.operations;

import com.jpexs.graphs.structure.BasicEditableMultiNode;
import com.jpexs.graphs.structure.nodes.EditableMultiNode;
import com.jpexs.graphs.structure.nodes.EditableNode;
import com.jpexs.graphs.structure.nodes.Node;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class MultiNodeJoiner<T extends EditableNode> {

    public EditableNode createMultiNodes(T head) {
        Collection<T> heads = new ArrayList<>();
        heads.add(head);
        Collection<EditableNode> multiHeads = createMultiNodes(heads);
        return multiHeads.toArray(new EditableNode[1])[0];
    }

    public Collection<EditableNode> createMultiNodes(Collection<T> heads) {
        Collection<EditableNode> ret = new ArrayList<>();
        for (EditableNode head : heads) {
            ret.add(createMultiNodes(head, new LinkedHashSet<>()));
        }
        return ret;
    }

    private EditableNode createMultiNodes(EditableNode node, Set<EditableNode> visited) {
        if (visited.contains(node)) {
            return node;
        }
        final EditableNode originalNode = node;
        EditableNode result;

        EditableNode currentNode = originalNode;
        List<Node> subNodesList = new ArrayList<>();
        subNodesList.add(currentNode);
        visited.add(currentNode);

        while (currentNode.getNext().size() == 1 && currentNode.getNext().get(0).getPrev().size() == 1 && !visited.contains(currentNode.getNext().get(0))) {
            currentNode = (EditableNode) currentNode.getNext().get(0);
            visited.add(currentNode);
            subNodesList.add(currentNode);
        }

        if (subNodesList.size() > 1) {
            Node lastSubNode = subNodesList.get(subNodesList.size() - 1);
            Node firstSubNode = subNodesList.get(0);

            List<String> subIds = new ArrayList<>();
            for (Node sub : subNodesList) {
                subIds.add(sub.getId());
            }
            String multiId = String.join("\\l", subIds) + "\\l";
            EditableMultiNode multiNode = new BasicEditableMultiNode(multiId);
            for (Node sub : subNodesList) {
                multiNode.addSubNode(sub);
            }
            //remove connection lastSubNode->after, add connection multiNode->after
            for (int i = 0; i < lastSubNode.getNext().size(); i++) {
                Node next = lastSubNode.getNext().get(i);
                multiNode.addNext(next);
                if (lastSubNode instanceof EditableNode) {  //it must be - TODO - make detector use only mutable
                    EditableNode lastSubNodeMutable = (EditableNode) lastSubNode;
                    lastSubNodeMutable.removeNext(next);
                    i--; //removing from iterated nexts, must decrement to not skip anything
                }
                if (next instanceof EditableNode) { //it must be - TODO - make detector use only mutable
                    EditableNode nextMutable = (EditableNode) next;
                    for (int j = 0; j < next.getPrev().size(); j++) {
                        if (next.getPrev().get(j) == lastSubNode) {
                            nextMutable.setPrev(j, multiNode);
                        }
                    }
                }
            }
            //remove connection before->firstNode, add connection before->multiNode
            for (int i = 0; i < firstSubNode.getPrev().size(); i++) {
                Node prev = firstSubNode.getPrev().get(i);
                multiNode.addPrev(prev);
                if (firstSubNode instanceof EditableNode) { //it must be - TODO - make detector use only mutable
                    EditableNode firstSubNodeMutable = (EditableNode) firstSubNode;
                    firstSubNodeMutable.removePrev(prev);
                    i--; //removing from iterated prevs, must decrement to not skip anything
                }
                if (prev instanceof EditableNode) { //it must be - TODO - make detector use only mutable
                    EditableNode prevMutable = (EditableNode) prev;
                    for (int j = 0; j < prev.getNext().size(); j++) {
                        if (prev.getNext().get(j) == firstSubNode) {
                            prevMutable.setNext(j, multiNode);
                        }
                    }
                }
            }
            fireMultiNodeJoined(multiNode);
            fireStep();
            result = multiNode;
        } else {
            result = originalNode;
        }

        fireStep();
        for (Node next : result.getNext()) {
            createMultiNodes((EditableNode) next, visited);
        }

        return result;
    }

    private List<MultiNodeJoinerProgressListener> listeners = new ArrayList<>();

    public void addListener(MultiNodeJoinerProgressListener l) {
        listeners.add(l);
    }

    public void removeListener(MultiNodeJoinerProgressListener l) {
        listeners.remove(l);
    }

    private void fireMultiNodeJoined(EditableMultiNode node) {
        for (MultiNodeJoinerProgressListener l : listeners) {
            l.multiNodeJoined(node);
        }
    }

    private void fireStep() {
        for (MultiNodeJoinerProgressListener l : listeners) {
            l.step();
        }
    }
}
