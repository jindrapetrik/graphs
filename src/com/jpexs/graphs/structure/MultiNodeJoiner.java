package com.jpexs.graphs.structure;

import com.jpexs.graphs.structure.nodes.MultiNode;
import com.jpexs.graphs.structure.nodes.MutableMultiNode;
import com.jpexs.graphs.structure.nodes.MutableNode;
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
public class MultiNodeJoiner<T extends MutableNode> {

    public MutableNode createMultiNodes(T head) {
        Collection<T> heads = new ArrayList<>();
        heads.add(head);
        Collection<MutableNode> multiHeads = createMultiNodes(heads);
        return multiHeads.toArray(new MutableNode[1])[0];
    }

    public Collection<MutableNode> createMultiNodes(Collection<T> heads) {
        Collection<MutableNode> ret = new ArrayList<>();
        for (MutableNode head : heads) {
            ret.add(createMultiNodes(head, new LinkedHashSet<>()));
        }
        return ret;
    }

    private MutableNode createMultiNodes(MutableNode node, Set<MutableNode> visited) {
        if (visited.contains(node)) {
            return node;
        }
        final MutableNode originalNode = node;
        MutableNode result;

        MutableNode currentNode = originalNode;
        List<Node> subNodesList = new ArrayList<>();
        subNodesList.add(currentNode);
        visited.add(currentNode);

        while (currentNode.getNext().size() == 1 && currentNode.getNext().get(0).getPrev().size() == 1 && !visited.contains(currentNode.getNext().get(0))) {
            currentNode = (MutableNode) currentNode.getNext().get(0);
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
            MutableMultiNode multiNode = new BasicMutableMultiNode(multiId);
            for (Node sub : subNodesList) {
                multiNode.addSubNode(sub);
            }
            //remove connection lastSubNode->after, add connection multiNode->after
            for (int i = 0; i < lastSubNode.getNext().size(); i++) {
                Node next = lastSubNode.getNext().get(i);
                multiNode.addNext(next);
                if (lastSubNode instanceof MutableNode) {  //it must be - TODO - make detector use only mutable
                    MutableNode lastSubNodeMutable = (MutableNode) lastSubNode;
                    lastSubNodeMutable.removeNext(next);
                    i--; //removing from iterated nexts, must decrement to not skip anything
                }
                if (next instanceof MutableNode) { //it must be - TODO - make detector use only mutable
                    MutableNode nextMutable = (MutableNode) next;
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
                if (firstSubNode instanceof MutableNode) { //it must be - TODO - make detector use only mutable
                    MutableNode firstSubNodeMutable = (MutableNode) firstSubNode;
                    firstSubNodeMutable.removePrev(prev);
                    i--; //removing from iterated prevs, must decrement to not skip anything
                }
                if (prev instanceof MutableNode) { //it must be - TODO - make detector use only mutable
                    MutableNode prevMutable = (MutableNode) prev;
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
            createMultiNodes((MutableNode) next, visited);
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

    private void fireMultiNodeJoined(MutableMultiNode node) {
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
