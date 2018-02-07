package graphs.unstructured;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import jdk.nashorn.internal.ir.IfNode;

/**
 *
 * @author JPEXS
 */
public class CodeStructureDetector {

    private List<Node> todoList = new ArrayList<>();
    private List<Node> alreadyProcessed = new ArrayList<>();
    private Map<Edge, List<Node>> decistionLists = new HashMap<>();
    private List<List<Node>> rememberedDecisionLists = new ArrayList<>();
    private List<Node> waiting = new ArrayList<>();
    private List<Node> loopContinues = new ArrayList<>();
    private List<Edge> gotoEdges = new ArrayList<>();
    private EndIfFactory endIfFactory = new BasicEndIfFactory();

    public void setEndIfFactory(EndIfFactory endIfFactory) {
        this.endIfFactory = endIfFactory;
    }

    private Set<Edge> ignoredEdges = new HashSet<>();

    public void detect(Node head, List<Node> loopContinues, List<Edge> gotoEdges) {
        Set<Node> heads = new HashSet<>();
        heads.add(head);
        detect(heads, loopContinues, gotoEdges);
    }

    public void detect(Collection<Node> heads, List<Node> loopContinues, List<Edge> gotoEdges) {
        todoList.addAll(heads);
        walk();
        loopContinues.addAll(this.loopContinues);
        gotoEdges.addAll(this.gotoEdges);
    }

    private boolean walk() {

        walkDecisionLists();
        fireNoNodeSelected();
        for (int i = 0; i < waiting.size(); i++) {
            Node cek = waiting.get(i);
            Set<Node> visited = new HashSet<>();
            Set<Node> insideLoopNodes = new HashSet<>();
            Set<Edge> loopExitEdges = new HashSet<>();
            Set<Edge> loopContinueEdges = new HashSet<>();

            if (leadsTo(cek, cek, insideLoopNodes, loopExitEdges, loopContinueEdges, visited)) { //it waits for self => loop

                Node continueNode = cek;
                for (Edge edge : loopContinueEdges) {
                    fireEdgeMarked(edge, EdgeType.BACK);
                }

                Set<Node> currentCekajici = new HashSet<>();
                currentCekajici.addAll(waiting);
                currentCekajici.remove(continueNode);
                loopContinues.add(continueNode);
                Set<Edge> cekajiciVstupniEdges = new HashSet<>();
                for (Node c : currentCekajici) {
                    for (Node pc : getPrevNodes(c)) {
                        cekajiciVstupniEdges.add(new Edge(pc, c));
                    }
                }
                ignoredEdges.addAll(loopContinueEdges);

                for (Node next : getNextNodes(continueNode)) {
                    if (!insideLoopNodes.contains(next)) {
                        cekajiciVstupniEdges.add(new Edge(continueNode, next));
                        currentCekajici.add(next);
                    }
                }

                ignoredEdges.addAll(cekajiciVstupniEdges); //zakonzervovat cekajici

                waiting.clear();
                todoList.add(continueNode);
                walk();
                ignoredEdges.removeAll(cekajiciVstupniEdges);
                alreadyProcessed.removeAll(currentCekajici);

                List<Node> loopDecisionList = Collections.unmodifiableList(calculateDecisionListFromPrevNodes(continueNode, getPrevNodes(continueNode) /*bez ignored*/));

                for (Edge edge : cekajiciVstupniEdges) {
                    if (alreadyProcessed.contains(edge.from) && !decistionLists.containsKey(edge)) {
                        //doplnit DL
                        decistionLists.put(edge, loopDecisionList);
                    }
                }
                if (currentCekajici.isEmpty()) {
                    return true;
                }
                todoList.addAll(currentCekajici);
                walk();
                return true;
            }
        }
        return false;
    }

    private boolean leadsTo(Node nodeSearchIn, Node nodeSearchWhich, Set<Node> insideLoopNodes, Set<Edge> noLeadEdges, Set<Edge> foundEdges, Set<Node> visited) {
        if (visited.contains(nodeSearchIn)) {
            return insideLoopNodes.contains(nodeSearchIn);
        }
        visited.add(nodeSearchIn);
        for (Node next : getNextNodes(nodeSearchIn)) {
            if (next.equals(nodeSearchWhich)) {
                foundEdges.add(new Edge(nodeSearchIn, next));
                return true;
            }
        }
        boolean ret = false;
        Set<Edge> currentNoLeadNodes = new HashSet<>();
        for (Node next : getNextNodes(nodeSearchIn)) {
            if (leadsTo(next, nodeSearchWhich, insideLoopNodes, currentNoLeadNodes, foundEdges, visited)) {
                insideLoopNodes.add(next);
                ret = true;
            } else {
                currentNoLeadNodes.add(new Edge(nodeSearchIn, next));
            }
        }
        if (ret == true) {
            noLeadEdges.addAll(currentNoLeadNodes);
        }
        return ret;
    }

    private void removeExitPointFromPrevDlists(Node node, Node exitPoint, Set<Node> processedNodes) {
        boolean lastOne = false;
        if (processedNodes.contains(node)) {
            return;
        }
        processedNodes.add(node);
        for (Node prev : node.getPrev()) {
            if (exitPoint.equals(prev)) {
                lastOne = true;
                break;
            }
        }

        for (Node prev : node.getPrev()) {
            Edge edge = new Edge(prev, node);
            List<Node> decisionList = decistionLists.get(edge);
            if (decisionList != null) {
                if (!decisionList.isEmpty() && decisionList.get(decisionList.size() - 1).equals(exitPoint)) {
                    List<Node> truncDecisionList = new ArrayList<>();
                    truncDecisionList.addAll(decisionList);
                    truncDecisionList.remove(truncDecisionList.size() - 1);
                    decistionLists.put(edge, truncDecisionList);
                }
            }
        }
        if (!lastOne) {
            for (Node prev : node.getPrev()) {
                if (loopContinues.contains(prev)) {
                    continue;
                }
                removeExitPointFromPrevDlists(prev, exitPoint, processedNodes);
            }
        }
    }

    private List<Node> calculateDecisionListFromPrevNodes(Node BOD, List<Node> prevNodes) {
        List<Node> nextDecisionList;
        List<List<Node>> prevDecisionLists = new ArrayList<>();
        List<Node> decisionListNodes = new ArrayList<>(prevNodes);
        for (Node prevNode : prevNodes) {
            Edge edge = new Edge(prevNode, BOD);
            List<Node> prevDL = decistionLists.get(edge);
            if (prevDL == null) {
                System.err.println("WARNING - no decisionList for edge " + edge);
            }
            prevDecisionLists.add(prevDL);
        }

        if (prevDecisionLists.isEmpty()) {
            nextDecisionList = new ArrayList<>();
        } else if (prevDecisionLists.size() == 1) {
            nextDecisionList = new ArrayList<>(prevDecisionLists.get(0));
        } else {
            //Vyjmout decisionListy, které mám z minula zapamatované jako nestrukturované
            for (int i = prevDecisionLists.size() - 1; i >= 0; i--) {
                if (rememberedDecisionLists.contains(prevDecisionLists.get(i))) {
                    Edge gotoEdge = new Edge(prevNodes.get(i), BOD);
                    gotoEdges.add(gotoEdge);
                    fireEdgeMarked(gotoEdge, EdgeType.GOTO);
                    prevDecisionLists.remove(i);
                }
            }

            loopcheck:
            while (true) {

                //search for same decision lists, join them to endif
                for (int i = 0; i < prevDecisionLists.size(); i++) {
                    List<Node> decisionListI = prevDecisionLists.get(i);
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
                            List<Node> decisionListJ = prevDecisionLists.get(j);
                            if (decisionListJ.equals(decisionListI)) {
                                sameIndices.add(j);
                            }
                        }
                    }
                    int numSame = sameIndices.size();
                    if (numSame > 1) { //Actually, there can be more than 2 branches - it's not an if, but... it's kind of structured...
                        Node decisionNode = decisionListI.get(decisionListI.size() - 1);
                        int numBranches = getNextNodes(decisionNode).size();
                        if (numSame == numBranches) {
                            List<Node> shorterDecisionList = new ArrayList<>(decisionListI);
                            shorterDecisionList.remove(shorterDecisionList.size() - 1);
                            List<Node> endIfPrevNodes = new ArrayList<>();
                            for (int index : sameIndices) {
                                prevDecisionLists.remove(index);
                                Node prev = decisionListNodes.remove(index);
                                endIfPrevNodes.add(prev);
                            }

                            fireNoNodeSelected();
                            MutableEndIfNode endIfNode = injectEndIf(decisionNode, endIfPrevNodes, BOD);
                            alreadyProcessed.add(endIfNode);
                            decisionListNodes.add(endIfNode);
                            prevDecisionLists.add(shorterDecisionList);
                            decistionLists.put(new Edge(endIfNode, BOD), shorterDecisionList);
                            fireEndIfNodeAdded(endIfNode);
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
                //- this means that one branch of ifblock does not finish in endif - it might be return / continue / break or some unstructured goto
                loopsize:
                for (int findSize = maxDecListSize; findSize > 1; findSize--) {
                    for (int j = 0; j < prevDecisionLists.size(); j++) {
                        List<Node> decisionListJ = prevDecisionLists.get(j);
                        if (decisionListJ.size() == findSize) {
                            for (int k = 0; k < prevDecisionLists.size(); k++) {
                                if (j == k) {
                                    continue;
                                }
                                List<Node> decisionListK = prevDecisionLists.get(k);
                                if (decisionListK.size() == findSize - 1) {
                                    List<Node> decisionListJKratsi = new ArrayList<>();
                                    decisionListJKratsi.addAll(decisionListJ);
                                    decisionListJKratsi.remove(decisionListJKratsi.size() - 1);
                                    if (decisionListJKratsi.equals(decisionListK)) {
                                        rememberedDecisionLists.add(decisionListJ);
                                        prevDecisionLists.set(j, Collections.unmodifiableList(decisionListJKratsi));
                                        decistionLists.put(new Edge(decisionListNodes.get(j), BOD), decisionListJKratsi);
                                        Node decisionNode = decisionListK.get(decisionListK.size() - 1);
                                        Node exitNode = decisionListJ.get(decisionListJ.size() - 1);

                                        //----
                                        List<Node> endIfPrevNodes = new ArrayList<>();
                                        int higherIndex = j > k ? j : k;
                                        int lowerIndex = j < k ? j : k;

                                        Node longerPrev = decisionListNodes.get(j);

                                        //Trick: remove higher index first. If we removed the lower first, higher indices would change.
                                        prevDecisionLists.remove(higherIndex);
                                        endIfPrevNodes.add(decisionListNodes.remove(higherIndex));
                                        prevDecisionLists.remove(lowerIndex);
                                        endIfPrevNodes.add(decisionListNodes.remove(lowerIndex));

                                        fireNoNodeSelected();

                                        List<Node> shorterDecisionList = new ArrayList<>(decisionListK);
                                        shorterDecisionList.remove(shorterDecisionList.size() - 1);
                                        MutableEndIfNode endIfNode = injectEndIf(decisionNode, endIfPrevNodes, BOD);
                                        alreadyProcessed.add(endIfNode);
                                        decisionListNodes.add(endIfNode);
                                        prevDecisionLists.add(shorterDecisionList);
                                        decistionLists.put(new Edge(endIfNode, BOD), shorterDecisionList);
                                        fireEndIfNodeAdded(endIfNode);
                                        //----
                                        fireUpdateDecisionLists(decistionLists);
                                        fireStep();
                                        removeExitPointFromPrevDlists(longerPrev, exitNode, new HashSet<>());
                                        fireUpdateDecisionLists(decistionLists);
                                        fireStep();
                                        continue loopcheck;
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            } //loopcheck

            if (prevDecisionLists.isEmpty()) {
                nextDecisionList = new ArrayList<>();
            } else if (prevDecisionLists.size() == 1) {
                nextDecisionList = new ArrayList<>(prevDecisionLists.get(0));
            } else {
                for (List<Node> zap : prevDecisionLists) {
                    //System.err.println("Pamatuji si: " + String.join(", ", zap));
                }
                rememberedDecisionLists.addAll(prevDecisionLists);
                List<Node> prefix = new ArrayList<>();
                Node nextPismeno;
                int pocetVPrefixu = 0;
                looppocet:
                while (true) {
                    nextPismeno = null;
                    for (List<Node> decisionList : prevDecisionLists) {
                        if (decisionList.size() == pocetVPrefixu) {
                            break looppocet;
                        }
                        if (nextPismeno == null) {
                            nextPismeno = decisionList.get(pocetVPrefixu);
                        }
                        if (!decisionList.get(pocetVPrefixu).equals(nextPismeno)) {
                            break looppocet;
                        }
                    }
                    prefix.add(nextPismeno);
                    pocetVPrefixu++;
                }
                Node decisionNode = null;
                if (!prefix.isEmpty()) {
                    decisionNode = prefix.remove(prefix.size() - 1);
                }
                for (int i = 0; i < prevDecisionLists.size(); i++) {
                    List<Node> decisionList = prevDecisionLists.get(i);
                    Node exitNode = decisionList.get(prefix.size() + 1);
                    removeExitPointFromPrevDlists(BOD, exitNode, new HashSet<>());
                }

                MutableEndIfNode endIfNode = injectEndIf(decisionNode, decisionListNodes, BOD);

                alreadyProcessed.add(endIfNode);
                //decisionListNodes.add(endIfNode);
                //prevDecisionLists.add(prefix);
                decistionLists.put(new Edge(endIfNode, BOD), prefix);
                fireEndIfNodeAdded(endIfNode);
                nextDecisionList = prefix;
            }
        }
        return nextDecisionList;
    }

    private List<Node> getPrevNodes(Node sourceNode) {
        List<Node> ret = new ArrayList<>();
        for (Node prev : sourceNode.getPrev()) {
            if (!ignoredEdges.contains(new Edge(prev, sourceNode))) {
                ret.add(prev);
            }
        }
        return ret;
    }

    private List<Node> getNextNodes(Node sourceNode) {
        List<Node> ret = new ArrayList<>();
        for (Node next : sourceNode.getNext()) {
            if (!ignoredEdges.contains(new Edge(sourceNode, next))) {
                ret.add(next);
            }
        }
        return ret;
    }

    private void walkDecisionLists() {
        do {
            Node BOD = todoList.remove(0);
            if (alreadyProcessed.contains(BOD)) {
                continue;
            }
            List<Node> prevNodes = getPrevNodes(BOD);
            boolean vsechnyPrevZpracovane = true;
            for (Node prevNode : prevNodes) {
                if (!alreadyProcessed.contains(prevNode)) {
                    vsechnyPrevZpracovane = false;
                    break;
                }
            }

            if (!vsechnyPrevZpracovane) {
                if (!waiting.contains(BOD)) {
                    waiting.add(BOD);
                }
            } else {
                waiting.remove(BOD);
                List<Node> mergedDecisionList = calculateDecisionListFromPrevNodes(BOD, prevNodes);
                alreadyProcessed.add(BOD);
                List<Node> nextNodes = getNextNodes(BOD);

                List<Node> nextDecisionList = mergedDecisionList;
                if (nextNodes.size() > 1) {
                    nextDecisionList.add(BOD);
                }

                for (Node next : nextNodes) {
                    Edge edge = new Edge(BOD, next);
                    decistionLists.put(edge, Collections.unmodifiableList(nextDecisionList));
                    todoList.add(next);
                }
                fireNodeSelected(BOD);
                fireUpdateDecisionLists(decistionLists);
                fireStep();
            }
        } while (!todoList.isEmpty());
    }

    private MutableEndIfNode injectEndIf(Node decisionNode, List<Node> prevNodes, Node afterNode) {
        List<MutableNode> prevMutables = new ArrayList<>();
        if (!(afterNode instanceof MutableNode)) {
            return null;
        }
        MutableNode afterNodeMutable = (MutableNode) afterNode;

        for (Node prev : prevNodes) {
            if (prev instanceof MutableNode) {
                prevMutables.add((MutableNode) prev);
            } else {
                return null;
            }
        }
        MutableEndIfNode endIfNode = endIfFactory.makeEndIfNode(decisionNode);
        endIfNode.addNext(afterNode);
        for (MutableNode prev : prevMutables) {
            prev.removeNext(afterNode);
            afterNodeMutable.removePrev(prev);
        }
        for (MutableNode prev : prevMutables) {
            endIfNode.addPrev(prev);
            prev.addNext(endIfNode);
        }

        for (MutableNode prev : prevMutables) {
            decistionLists.put(new Edge(prev, endIfNode), decistionLists.get(new Edge(prev, afterNode)));
        }

        return endIfNode;
    }

    private List<CodeStructureDetectorProgressListener> listeners = new ArrayList<>();

    public void addListener(CodeStructureDetectorProgressListener l) {
        listeners.add(l);
    }

    public void removeListener(CodeStructureDetectorProgressListener l) {
        listeners.remove(l);
    }

    private void fireEndIfNodeAdded(EndIfNode node) {
        for (CodeStructureDetectorProgressListener l : listeners) {
            l.endIfAdded(node);
        }
    }

    private void fireStep() {
        for (CodeStructureDetectorProgressListener l : listeners) {
            l.step();
        }
    }

    private void fireEdgeMarked(Edge edge, EdgeType edgeType) {
        for (CodeStructureDetectorProgressListener l : listeners) {
            l.edgeMarked(edge, edgeType);
        }
    }

    private void fireNodeSelected(Node node) {
        for (CodeStructureDetectorProgressListener l : listeners) {
            l.nodeSelected(node);
        }
    }

    private void fireUpdateDecisionLists(Map<Edge, List<Node>> decistionLists) {
        for (CodeStructureDetectorProgressListener l : listeners) {
            l.updateDecisionLists(decistionLists);
        }
    }

    private void fireNoNodeSelected() {
        for (CodeStructureDetectorProgressListener l : listeners) {
            l.noNodeSelected();
        }
    }
}
