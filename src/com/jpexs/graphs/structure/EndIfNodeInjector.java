/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpexs.graphs.structure;

import com.jpexs.graphs.structure.nodes.MutableEndIfNode;
import com.jpexs.graphs.structure.nodes.MutableNode;
import com.jpexs.graphs.structure.nodes.Node;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class EndIfNodeInjector<T extends MutableNode> {

    private EndIfFactory endIfFactory = new BasicEndIfFactory();

    public void setEndIfFactory(EndIfFactory endIfFactory) {
        this.endIfFactory = endIfFactory;
    }

    public MutableEndIfNode injectEndIf(T decisionNode, List<T> endBranchNodes, T afterNode) {
        int afterNodePrevIndex = Integer.MAX_VALUE;
        for (Node prev : endBranchNodes) {
            int index = afterNode.getPrev().indexOf(prev);
            if (index < afterNodePrevIndex) {
                afterNodePrevIndex = index;
            }
        }

        MutableEndIfNode endIfNode = endIfFactory.makeEndIfNode(decisionNode);

        //remove connection prev->after
        for (int i = 0; i < endBranchNodes.size(); i++) {
            MutableNode prev = endBranchNodes.get(i);
            prev.removeNext(afterNode);
        }
        for (MutableNode prev : endBranchNodes) {
            afterNode.removePrev(prev);
        }

        //add connection prev->endif 
        for (int i = 0; i < endBranchNodes.size(); i++) {
            MutableNode prev = endBranchNodes.get(i);
            prev.addNext(endIfNode);
            endIfNode.addPrev(prev);
        }
        //add connection endif->after
        endIfNode.addNext(afterNode);
        afterNode.addPrev(afterNodePrevIndex, endIfNode); //add to correct index

        fireEndIfAdded(endIfNode);
        return endIfNode;
    }

    private List<EnfIfNodeInjectorProgressListener> listeners = new ArrayList<>();

    public void addListener(EnfIfNodeInjectorProgressListener l) {
        listeners.add(l);
    }

    public void removeListener(EnfIfNodeInjectorProgressListener l) {
        listeners.remove(l);
    }

    private void fireEndIfAdded(MutableEndIfNode node) {
        for (EnfIfNodeInjectorProgressListener l : listeners) {
            l.endIfAdded(node);
        }
    }

}
