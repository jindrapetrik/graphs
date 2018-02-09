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
package com.jpexs.graphs.structure.factories.operations;

import com.jpexs.graphs.structure.Decision;
import com.jpexs.graphs.structure.DecisionList;
import com.jpexs.graphs.structure.Edge;
import com.jpexs.graphs.structure.nodes.Node;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author JPEXS
 */
public class CodeStructureDetector<T extends Node> {

    private List<T> todoList = new ArrayList<>();
    private List<Node> alreadyProcessed = new ArrayList<>();
    private Map<Edge<T>, DecisionList<T>> decistionLists = new HashMap<>();
    private List<DecisionList<T>> rememberedDecisionLists = new ArrayList<>();
    private List<T> waiting = new ArrayList<>();
    private List<Node> loopContinues = new ArrayList<>();
    private List<Edge<T>> backEdges = new ArrayList<>();
    private List<Edge<T>> gotoEdges = new ArrayList<>();
    private List<Edge<T>> exitIfEdges = new ArrayList<>();
    private Set<Edge<T>> ignoredEdges = new LinkedHashSet<>();

    public Node detect(T head, List<Node> loopContinues, List<Edge<T>> gotoEdges, List<Edge<T>> backEdges, List<Edge<T>> exitIfEdges) {
        Set<T> heads = new LinkedHashSet<>();
        heads.add(head);
        Collection<T> multiHeads = detect(heads, loopContinues, gotoEdges, backEdges, exitIfEdges);
        return multiHeads.toArray(new Node[1])[0];
    }

    public Collection<T> detect(Collection<T> heads, List<Node> loopContinues, List<Edge<T>> gotoEdges, List<Edge<T>> backEdges, List<Edge<T>> exitIfEdges) {
        //Collection<Node> multiHeads = createMultiNodes(heads);
        todoList.addAll(heads);
        walk();
        loopContinues.addAll(this.loopContinues);
        gotoEdges.addAll(this.gotoEdges);
        backEdges.addAll(this.backEdges);
        exitIfEdges.addAll(this.exitIfEdges);
        return heads;
    }

    private boolean walk() {

        walkDecisionLists();
        fireNoNodeSelected();
        for (int i = 0; i < waiting.size(); i++) {
            T cek = waiting.get(i);
            Set<Node> visited = new LinkedHashSet<>();
            Set<Node> insideLoopNodes = new LinkedHashSet<>();
            Set<Edge<T>> loopExitEdges = new LinkedHashSet<>();
            Set<Edge<T>> loopContinueEdges = new LinkedHashSet<>();

            if (leadsTo(cek, cek, insideLoopNodes, loopExitEdges, loopContinueEdges, visited)) { //it waits for self => loop

                T continueNode = cek;
                for (Edge<T> edge : loopContinueEdges) {
                    fireEdgeMarked(edge, DetectedEdgeType.BACK);
                }

                Set<T> currentWaiting = new LinkedHashSet<>();
                currentWaiting.addAll(waiting);
                currentWaiting.remove(continueNode);
                loopContinues.add(continueNode);
                backEdges.addAll(loopContinueEdges);
                Set<Edge<T>> cekajiciVstupniEdges = new LinkedHashSet<>();
                for (T c : currentWaiting) {
                    for (T pc : getPrevNodes(c)) {
                        cekajiciVstupniEdges.add(new Edge<>(pc, c));
                    }
                }
                ignoredEdges.addAll(loopContinueEdges);

                for (T next : getNextNodes(continueNode)) {
                    if (!insideLoopNodes.contains(next)) {
                        cekajiciVstupniEdges.add(new Edge<>(continueNode, next));
                        currentWaiting.add(next);
                    }
                }

                ignoredEdges.addAll(cekajiciVstupniEdges); //zakonzervovat cekajici

                waiting.clear();
                todoList.add(continueNode);
                walk();
                ignoredEdges.removeAll(cekajiciVstupniEdges);
                alreadyProcessed.removeAll(currentWaiting);

                DecisionList<T> loopDecisionList = calculateDecisionListFromPrevNodes(continueNode, getPrevNodes(continueNode) /*bez ignored*/).lockForChanges();

                for (Edge<T> edge : cekajiciVstupniEdges) {
                    if (alreadyProcessed.contains(edge.from) && !decistionLists.containsKey(edge)) {
                        //doplnit DL
                        decistionLists.put(edge, loopDecisionList);
                    }
                }
                if (currentWaiting.isEmpty()) {
                    return true;
                }
                todoList.addAll(currentWaiting);
                walk();
                return true;
            }
        }
        return false;
    }

    private boolean leadsTo(T nodeSearchIn, T nodeSearchWhich, Set<Node> insideLoopNodes, Set<Edge<T>> noLeadEdges, Set<Edge<T>> foundEdges, Set<Node> visited) {
        if (visited.contains(nodeSearchIn)) {
            return insideLoopNodes.contains(nodeSearchIn);
        }
        visited.add(nodeSearchIn);
        for (Node next : getNextNodes(nodeSearchIn)) {
            @SuppressWarnings("unchecked")
            T nextT = (T) next;
            if (next.equals(nodeSearchWhich)) {
                foundEdges.add(new Edge<>(nodeSearchIn, nextT));
                return true;
            }
        }
        boolean ret = false;
        Set<Edge<T>> currentNoLeadNodes = new LinkedHashSet<>();
        for (T next : getNextNodes(nodeSearchIn)) {
            if (leadsTo(next, nodeSearchWhich, insideLoopNodes, currentNoLeadNodes, foundEdges, visited)) {
                insideLoopNodes.add(next);
                ret = true;
            } else {
                currentNoLeadNodes.add(new Edge<>(nodeSearchIn, next));
            }
        }
        if (ret == true) {
            noLeadEdges.addAll(currentNoLeadNodes);
        }
        return ret;
    }

    private boolean removeExitPointFromPrevDlists(T prevNode, T node, T exitPoint, Set<T> processedNodes) {
        boolean lastOne = false;
        if (processedNodes.contains(node)) {
            return false;
        }
        processedNodes.add(node);
        if (exitPoint.equals(prevNode)) {
            int insideIfBranchIndex = prevNode.getNext().indexOf(node);
            for (int branchIndex = 0; branchIndex < exitPoint.getNext().size(); branchIndex++) {
                if (branchIndex != insideIfBranchIndex) {
                    @SuppressWarnings("unchecked")
                    T branchNodeT = (T) exitPoint.getNext().get(branchIndex);

                    Edge<T> exitEdge = new Edge<>(exitPoint, branchNodeT);
                    exitIfEdges.add(exitEdge);
                    fireEdgeMarked(exitEdge, DetectedEdgeType.OUTSIDEIF);
                }
            }

            return true;
        }
        Edge<T> edge = new Edge<>(prevNode, node);
        DecisionList<T> decisionList = decistionLists.get(edge);
        if (decisionList != null) {
            if (!decisionList.isEmpty() && decisionList.get(decisionList.size() - 1).getIfNode().equals(exitPoint)) {
                DecisionList<T> truncDecisionList = new DecisionList<>();
                truncDecisionList.addAll(decisionList);
                truncDecisionList.remove(truncDecisionList.size() - 1);
                decistionLists.put(edge, truncDecisionList);
            }
        }

        if (!lastOne) {
            for (Node prev : prevNode.getPrev()) {
                if (loopContinues.contains(prev)) {
                    continue;
                }
                @SuppressWarnings("unchecked")
                T prevT = (T) prev;
                if (removeExitPointFromPrevDlists(prevT, prevNode, exitPoint, processedNodes)) {
                    return true;
                }
            }
        }
        return false;
    }

    private DecisionList<T> calculateDecisionListFromPrevNodes(T BOD, List<T> prevNodes) {
        DecisionList<T> nextDecisionList;
        List<DecisionList<T>> prevDecisionLists = new ArrayList<>();
        List<T> decisionListNodes = new ArrayList<>(prevNodes);
        for (T prevNode : prevNodes) {
            Edge<T> edge = new Edge<>(prevNode, BOD);
            DecisionList<T> prevDL = decistionLists.get(edge);
            if (prevDL == null) {
                System.err.println("WARNING - no decisionList for edge " + edge);
            }
            prevDecisionLists.add(prevDL);
        }

        if (prevDecisionLists.isEmpty()) {
            nextDecisionList = new DecisionList<>();
        } else if (prevDecisionLists.size() == 1) {
            nextDecisionList = new DecisionList<>(prevDecisionLists.get(0));
        } else {
            //Remove decisionLists, which are remembered from last time as unstructured
            for (int i = prevDecisionLists.size() - 1; i >= 0; i--) {
                if (rememberedDecisionLists.contains(prevDecisionLists.get(i))) {
                    Edge<T> gotoEdge = new Edge<>(prevNodes.get(i), BOD);
                    gotoEdges.add(gotoEdge);
                    fireEdgeMarked(gotoEdge, DetectedEdgeType.GOTO);
                    prevDecisionLists.remove(i);
                }
            }

            loopcheck:
            while (true) {

                //search for same decision lists, join them to endif
                for (int i = 0; i < prevDecisionLists.size(); i++) {
                    DecisionList<T> decisionListI = prevDecisionLists.get(i);
                    Set<Integer> sameIndices = new TreeSet<>(new Comparator<Integer>() {
                        @Override
                        public int compare(Integer o1, Integer o2) {
                            return o2 - o1;
                        }
                    });
                    sameIndices.add(i);
                    if (decisionListI.isEmpty()) {
                        continue;
                    }
                    for (int j = 0; j < prevDecisionLists.size(); j++) {
                        if (i != j) {
                            DecisionList<T> decisionListJ = prevDecisionLists.get(j);
                            if (decisionListJ.ifNodesEquals(decisionListI)) {
                                sameIndices.add(j);
                            }
                        }
                    }
                    int numSame = sameIndices.size();
                    if (numSame > 1) { //Actually, there can be more than 2 branches - it's not an if, but... it's kind of structured...
                        T decisionNode = decisionListI.get(decisionListI.size() - 1).getIfNode();
                        int numBranches = getNextNodes(decisionNode).size();
                        if (numSame == numBranches) {
                            DecisionList<T> shorterDecisionList = new DecisionList<>(decisionListI);
                            shorterDecisionList.remove(shorterDecisionList.size() - 1);
                            List<T> endBranchNodes = new ArrayList<>();
                            for (int index : sameIndices) {
                                Decision<T> decision = prevDecisionLists.get(index).get(decisionListI.size() - 1);
                                int branchNum = decision.getBranchNum();
                                prevDecisionLists.remove(index);
                                T prev = decisionListNodes.remove(index);
                                if (branchNum == 0) {
                                    endBranchNodes.add(0, prev);
                                } else {
                                    endBranchNodes.add(prev); //indices are in reverse order, make this list too
                                }
                            }

                            fireNoNodeSelected();
                            T endIfNode = fireEndIfDetected(decisionNode, endBranchNodes, BOD);

                            alreadyProcessed.add(endIfNode);
                            decisionListNodes.add(endIfNode);
                            prevDecisionLists.add(shorterDecisionList);
                            decistionLists.put(new Edge<>(endIfNode, BOD), shorterDecisionList);
                            //fireEndIfNodeAdded(endIfNode);
                            fireUpdateDecisionLists(decistionLists);
                            fireStep();
                            continue loopcheck;
                        }
                    }
                }

                int maxDecListSize = 0;
                for (int i = 0; i < prevDecisionLists.size(); i++) {
                    int size = prevDecisionLists.get(i).size();
                    if (size > maxDecListSize) {
                        maxDecListSize = size;
                    }
                }

                //- order decisionLists by their size, descending
                //- search for decisionlist K, and J, decisionlist J has is same as K and has one more added node
                //- replace the longer one with the shorter version
                //- this means that one branch of ifblock does not finish in endif - it might be return / continue / break or some unstructured goto,
                //- we call that edge an ExitEdge of the if
                loopsize:
                for (int findSize = maxDecListSize; findSize > 1; findSize--) {
                    for (int j = 0; j < prevDecisionLists.size(); j++) {
                        DecisionList<T> decisionListJ = prevDecisionLists.get(j);
                        if (decisionListJ.size() == findSize) {
                            for (int k = 0; k < prevDecisionLists.size(); k++) {
                                if (j == k) {
                                    continue;
                                }
                                DecisionList<T> decisionListK = prevDecisionLists.get(k);
                                if (decisionListK.size() == findSize - 1) {
                                    DecisionList<T> decisionListJKratsi = new DecisionList<>();
                                    decisionListJKratsi.addAll(decisionListJ);
                                    decisionListJKratsi.remove(decisionListJKratsi.size() - 1);
                                    if (decisionListJKratsi.ifNodesEquals(decisionListK)) {
                                        rememberedDecisionLists.add(decisionListJ);
                                        prevDecisionLists.set(j, decisionListJKratsi.lockForChanges());
                                        decistionLists.put(new Edge<>(decisionListNodes.get(j), BOD), decisionListJKratsi);
                                        Decision<T> decisionK = decisionListK.get(decisionListK.size() - 1);
                                        Decision<T> decisionJ = decisionListJ.get(decisionListJKratsi.size() - 1);

                                        T decisionNode = decisionK.getIfNode();

                                        Decision<T> exitDecision = decisionListJ.get(decisionListJ.size() - 1);
                                        T exitNode = exitDecision.getIfNode();

                                        //----
                                        List<T> endBranchNodes = new ArrayList<>();
                                        int higherIndex = j > k ? j : k;
                                        int lowerIndex = j < k ? j : k;

                                        T longerPrev = decisionListNodes.get(j);

                                        //Trick: remove higher index first. If we removed the lower first, higher indices would change.
                                        prevDecisionLists.remove(higherIndex);
                                        prevDecisionLists.remove(lowerIndex);
                                        T prevNodeK = decisionListNodes.get(k);
                                        T prevNodeJ = decisionListNodes.get(j);
                                        decisionListNodes.remove(higherIndex);
                                        decisionListNodes.remove(lowerIndex);

                                        endBranchNodes.add(decisionJ.getBranchNum() == 0 ? prevNodeJ : prevNodeK);
                                        endBranchNodes.add(decisionK.getBranchNum() == 1 ? prevNodeK : prevNodeJ);

                                        fireNoNodeSelected();

                                        DecisionList<T> shorterDecisionList = new DecisionList<>(decisionListK);
                                        shorterDecisionList.remove(shorterDecisionList.size() - 1);
                                        //injecting if 2
                                        T endIfNode = fireEndIfDetected(decisionNode, endBranchNodes, BOD);
                                        alreadyProcessed.add(endIfNode);
                                        decisionListNodes.add(endIfNode);
                                        prevDecisionLists.add(shorterDecisionList);
                                        decistionLists.put(new Edge<>(endIfNode, BOD), shorterDecisionList);
                                        //----
                                        fireUpdateDecisionLists(decistionLists);
                                        fireStep();

                                        removeExitPointFromPrevDlists(longerPrev, endIfNode, exitNode, new LinkedHashSet<>());

                                        fireUpdateDecisionLists(decistionLists);
                                        fireStep();
                                        continue loopcheck;
                                    }
                                }
                            }
                        }
                    }
                }
                break; //if no more left found, exit loop
            } //loopcheck

            if (prevDecisionLists.isEmpty()) { //no more prevNodes left
                nextDecisionList = new DecisionList<>();
            } else if (prevDecisionLists.size() == 1) { //onePrev node
                nextDecisionList = new DecisionList<>(prevDecisionLists.get(0));
            } else {
                //more prevNodes remaining

                DecisionList<T> prefix = new DecisionList<>();
                Decision<T> nextDecision;
                int numInPrefix = 0;
                looppocet:
                while (true) {
                    nextDecision = null;
                    for (DecisionList<T> decisionList : prevDecisionLists) {
                        if (decisionList.size() == numInPrefix) {
                            break looppocet;
                        }

                        Decision<T> currentDecision = decisionList.get(numInPrefix);
                        if (nextDecision == null) {
                            nextDecision = currentDecision;
                        }
                        if (!currentDecision.getIfNode().equals(nextDecision.getIfNode())) {
                            break looppocet;
                        }
                    }
                    prefix.add(nextDecision);
                    numInPrefix++;
                }
                for (int i = 0; i < prevDecisionLists.size(); i++) {
                    DecisionList<T> decisionList = prevDecisionLists.get(i);
                    if (decisionList.size() > prefix.size()) {
                        rememberedDecisionLists.add(decisionList);
                        Edge<T> gotoEdge = new Edge<>(decisionListNodes.get(i), BOD);
                        gotoEdges.add(gotoEdge);
                        fireEdgeMarked(gotoEdge, DetectedEdgeType.GOTO);
                    }
                    if (decisionList.size() > prefix.size()) {
                        Decision<T> exitDecision = decisionList.get(prefix.size() - 1 + 1);
                        T exitNode = exitDecision.getIfNode();
                        removeExitPointFromPrevDlists(decisionListNodes.get(i), BOD, exitNode, new LinkedHashSet<>());
                    }
                }

                //just merge of unstructured branches
                for (Node prev : decisionListNodes) {
                    @SuppressWarnings("unchecked")
                    T prevT = (T) prev;
                    decistionLists.put(new Edge<>(prevT, BOD), prefix);
                }
                fireStep();
                nextDecisionList = prefix;
            }
        }
        return nextDecisionList;
    }

    private List<T> getPrevNodes(T sourceNode) {
        List<T> ret = new ArrayList<>();
        for (Node prev : sourceNode.getPrev()) {
            @SuppressWarnings("unchecked")
            T prevT = (T) prev;
            if (!ignoredEdges.contains(new Edge<>(prevT, sourceNode))) {
                ret.add((T) prevT);
            }
        }
        return ret;
    }

    private List<T> getNextNodes(T sourceNode) {
        List<T> ret = new ArrayList<>();
        for (Node next : sourceNode.getNext()) {
            @SuppressWarnings("unchecked")
            T nextT = (T) next;
            if (!ignoredEdges.contains(new Edge<>(sourceNode, nextT))) {
                ret.add((T) nextT);
            }
        }
        return ret;
    }

    private void walkDecisionLists() {
        do {
            T currentPoint = todoList.remove(0);
            if (alreadyProcessed.contains(currentPoint)) {
                continue;
            }
            List<T> prevNodes = getPrevNodes(currentPoint);
            boolean vsechnyPrevZpracovane = true;
            for (Node prevNode : prevNodes) {
                if (!alreadyProcessed.contains(prevNode)) {
                    vsechnyPrevZpracovane = false;
                    break;
                }
            }

            if (!vsechnyPrevZpracovane) {
                if (!waiting.contains(currentPoint)) {
                    waiting.add(currentPoint);
                }
            } else {
                waiting.remove(currentPoint);
                DecisionList<T> mergedDecisionList = calculateDecisionListFromPrevNodes(currentPoint, prevNodes);
                alreadyProcessed.add(currentPoint);
                List<T> nextNodes = getNextNodes(currentPoint);

                for (int branch = 0; branch < nextNodes.size(); branch++) {
                    T next = nextNodes.get(branch);
                    Edge<T> edge = new Edge<>(currentPoint, next);
                    DecisionList<T> nextDecisionList = new DecisionList<>(mergedDecisionList);
                    if (nextNodes.size() > 1) {
                        nextDecisionList.add(new Decision<>(currentPoint, branch));
                    }
                    decistionLists.put(edge, nextDecisionList.lockForChanges());
                    todoList.add(next);
                }
                fireNodeSelected(currentPoint);
                fireUpdateDecisionLists(decistionLists);
                fireStep();
            }
        } while (!todoList.isEmpty());
    }

    private List<CodeStructureDetectorProgressListener<T>> listeners = new ArrayList<>();

    public void addListener(CodeStructureDetectorProgressListener<T> l) {
        listeners.add(l);
    }

    public void removeListener(CodeStructureDetectorProgressListener<T> l) {
        listeners.remove(l);
    }

    private T fireEndIfDetected(T decisionNode, List<T> endBranchNodes, T node) {
        List<Edge<T>> beforeEdges = new ArrayList<>();
        for (T prev : endBranchNodes) {
            beforeEdges.add(new Edge<>(prev, node));
        }
        //T endIfNode = afterNode; //fireEndIfDetected(decisionNode, endBranchNodes, BOD);

        for (CodeStructureDetectorProgressListener<T> l : listeners) {
            node = l.endIfDetected(decisionNode, endBranchNodes, node);
        }

        //restore decisionlists of branches
        for (int m = 0; m < node.getPrev().size(); m++) {
            @SuppressWarnings("unchecked")
            T prev = (T) node.getPrev().get(m);
            decistionLists.put(new Edge<>(prev, node), decistionLists.get(beforeEdges.get(m)));
        }
        return node;
    }

    private void fireStep() {
        for (CodeStructureDetectorProgressListener<T> l : listeners) {
            l.step();
        }
    }

    private void fireEdgeMarked(Edge<T> edge, DetectedEdgeType edgeType) {
        for (CodeStructureDetectorProgressListener<T> l : listeners) {
            l.edgeMarked(edge, edgeType);
        }
    }

    private void fireNodeSelected(T node) {
        for (CodeStructureDetectorProgressListener<T> l : listeners) {
            l.nodeSelected(node);
        }
    }

    private void fireUpdateDecisionLists(Map<Edge<T>, DecisionList<T>> decistionLists) {
        for (CodeStructureDetectorProgressListener<T> l : listeners) {
            l.updateDecisionLists(decistionLists);
        }
    }

    private void fireNoNodeSelected() {
        for (CodeStructureDetectorProgressListener<T> l : listeners) {
            l.noNodeSelected();
        }
    }

}
