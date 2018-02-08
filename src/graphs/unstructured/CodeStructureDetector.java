package graphs.unstructured;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
public class CodeStructureDetector {

    private List<Node> todoList = new ArrayList<>();
    private List<Node> alreadyProcessed = new ArrayList<>();
    private Map<Edge, DecisionList> decistionLists = new HashMap<>();
    private List<DecisionList> rememberedDecisionLists = new ArrayList<>();
    private List<Node> waiting = new ArrayList<>();
    private List<Node> loopContinues = new ArrayList<>();
    private List<Edge> backEdges = new ArrayList<>();
    private List<Edge> gotoEdges = new ArrayList<>();
    private EndIfFactory endIfFactory = new BasicEndIfFactory();

    public void setEndIfFactory(EndIfFactory endIfFactory) {
        this.endIfFactory = endIfFactory;
    }

    private Set<Edge> ignoredEdges = new LinkedHashSet<>();

    private Collection<Node> createMultiNodes(Collection<Node> heads) {
        Collection<Node> ret = new ArrayList<>();
        for (Node head : heads) {
            ret.add(createMultiNodes(head, new LinkedHashSet<>()));
        }
        return ret;
    }

    private Node createMultiNodes(Node node, Set<Node> visited) {
        if (visited.contains(node)) {
            return node;
        }
        final Node originalNode = node;
        Node result;

        Node currentNode = originalNode;
        List<Node> subNodesList = new ArrayList<>();
        subNodesList.add(currentNode);
        visited.add(currentNode);

        while (currentNode.getNext().size() == 1 && currentNode.getNext().get(0).getPrev().size() == 1 && !visited.contains(currentNode.getNext().get(0))) {
            currentNode = currentNode.getNext().get(0);
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
            System.out.println("added multinode " + multiId);
            //TODO: ifif příklad
            MutableMultiNode multiNode = new BasicMutableMultiNode(multiId);
            for (Node sub : subNodesList) {
                multiNode.addSubNode(sub);
            }
            //přerušit vazby lastSubNode->after, přidat vazbu multiNode->after
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
            //přerušit vazby before->firstNode, přidat vazbu before->multiNode
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
            createMultiNodes(next, visited);
        }

        return result;
    }

    public Node detect(Node head, List<Node> loopContinues, List<Edge> gotoEdges) {
        Set<Node> heads = new LinkedHashSet<>();
        heads.add(head);
        Collection<Node> multiHeads = detect(heads, loopContinues, gotoEdges);
        return multiHeads.toArray(new Node[1])[0];
    }

    public Collection<Node> detect(Collection<Node> heads, List<Node> loopContinues, List<Edge> gotoEdges) {
        Collection<Node> multiHeads = createMultiNodes(heads);
        todoList.addAll(multiHeads);
        walk();
        loopContinues.addAll(this.loopContinues);
        gotoEdges.addAll(this.gotoEdges);
        return multiHeads;
    }

    private boolean walk() {

        walkDecisionLists();
        fireNoNodeSelected();
        for (int i = 0; i < waiting.size(); i++) {
            Node cek = waiting.get(i);
            Set<Node> visited = new LinkedHashSet<>();
            Set<Node> insideLoopNodes = new LinkedHashSet<>();
            Set<Edge> loopExitEdges = new LinkedHashSet<>();
            Set<Edge> loopContinueEdges = new LinkedHashSet<>();

            if (leadsTo(cek, cek, insideLoopNodes, loopExitEdges, loopContinueEdges, visited)) { //it waits for self => loop

                Node continueNode = cek;
                for (Edge edge : loopContinueEdges) {
                    fireEdgeMarked(edge, EdgeType.BACK);
                }

                Set<Node> currentCekajici = new LinkedHashSet<>();
                currentCekajici.addAll(waiting);
                currentCekajici.remove(continueNode);
                loopContinues.add(continueNode);
                backEdges.addAll(loopContinueEdges);
                Set<Edge> cekajiciVstupniEdges = new LinkedHashSet<>();
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

                DecisionList loopDecisionList = DecisionList.unmodifiableList(calculateDecisionListFromPrevNodes(continueNode, getPrevNodes(continueNode) /*bez ignored*/));

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
        Set<Edge> currentNoLeadNodes = new LinkedHashSet<>();
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
            DecisionList decisionList = decistionLists.get(edge);
            if (decisionList != null) {
                if (!decisionList.isEmpty() && decisionList.get(decisionList.size() - 1).getIfNode().equals(exitPoint)) {
                    DecisionList truncDecisionList = new DecisionList();
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

    private DecisionList calculateDecisionListFromPrevNodes(Node BOD, List<Node> prevNodes) {
        DecisionList nextDecisionList;
        List<DecisionList> prevDecisionLists = new ArrayList<>();
        List<Node> decisionListNodes = new ArrayList<>(prevNodes);
        for (Node prevNode : prevNodes) {
            Edge edge = new Edge(prevNode, BOD);
            DecisionList prevDL = decistionLists.get(edge);
            if (prevDL == null) {
                System.err.println("WARNING - no decisionList for edge " + edge);
            }
            prevDecisionLists.add(prevDL);
        }

        if (prevDecisionLists.isEmpty()) {
            nextDecisionList = new DecisionList();
        } else if (prevDecisionLists.size() == 1) {
            nextDecisionList = new DecisionList(prevDecisionLists.get(0));
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
                    DecisionList decisionListI = prevDecisionLists.get(i);
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
                            DecisionList decisionListJ = prevDecisionLists.get(j);
                            if (decisionListJ.ifNodesEquals(decisionListI)) {
                                sameIndices.add(j);
                            }
                        }
                    }
                    int numSame = sameIndices.size();
                    if (numSame > 1) { //Actually, there can be more than 2 branches - it's not an if, but... it's kind of structured...
                        Node decisionNode = decisionListI.get(decisionListI.size() - 1).getIfNode();
                        int numBranches = getNextNodes(decisionNode).size();
                        if (numSame == numBranches) {
                            DecisionList shorterDecisionList = new DecisionList(decisionListI);
                            shorterDecisionList.remove(shorterDecisionList.size() - 1);
                            List<Node> endBranchNodes = new ArrayList<>();
                            for (int index : sameIndices) {
                                Decision decision = prevDecisionLists.get(index).get(decisionListI.size() - 1);
                                int branchNum = decision.getBranchNum();
                                prevDecisionLists.remove(index);
                                Node prev = decisionListNodes.remove(index);
                                System.out.println("branchNum=" + branchNum);
                                if (branchNum == 0) {
                                    endBranchNodes.add(0, prev);
                                } else {
                                    endBranchNodes.add(prev); //indices are in reverse order, make this list too
                                }
                            }

                            fireNoNodeSelected();
                            System.out.println("injecting if 1");
                            MutableEndIfNode endIfNode = injectEndIf(decisionNode, endBranchNodes, BOD);
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
                        DecisionList decisionListJ = prevDecisionLists.get(j);
                        if (decisionListJ.size() == findSize) {
                            for (int k = 0; k < prevDecisionLists.size(); k++) {
                                if (j == k) {
                                    continue;
                                }
                                DecisionList decisionListK = prevDecisionLists.get(k);
                                if (decisionListK.size() == findSize - 1) {
                                    DecisionList decisionListJKratsi = new DecisionList();
                                    decisionListJKratsi.addAll(decisionListJ);
                                    decisionListJKratsi.remove(decisionListJKratsi.size() - 1);
                                    if (decisionListJKratsi.ifNodesEquals(decisionListK)) {
                                        rememberedDecisionLists.add(decisionListJ);
                                        prevDecisionLists.set(j, DecisionList.unmodifiableList(decisionListJKratsi));
                                        decistionLists.put(new Edge(decisionListNodes.get(j), BOD), decisionListJKratsi);
                                        Decision decisionK = decisionListK.get(decisionListK.size() - 1);
                                        Decision decisionJ = decisionListJ.get(decisionListJ.size() - 1);

                                        Node decisionNode = decisionK.getIfNode();
                                        Node exitNode = decisionJ.getIfNode();

                                        //----
                                        List<Node> endBranchNodes = new ArrayList<>();
                                        int higherIndex = j > k ? j : k;
                                        int lowerIndex = j < k ? j : k;

                                        Node longerPrev = decisionListNodes.get(j);

                                        //Trick: remove higher index first. If we removed the lower first, higher indices would change.
                                        prevDecisionLists.remove(higherIndex);
                                        prevDecisionLists.remove(lowerIndex);
                                        Node prevNodeK = decisionListNodes.get(k);
                                        Node prevNodeJ = decisionListNodes.get(j);
                                        decisionListNodes.remove(higherIndex);
                                        decisionListNodes.remove(lowerIndex);

                                        endBranchNodes.add(decisionJ.getBranchNum() == 0 ? prevNodeJ : prevNodeK);
                                        endBranchNodes.add(decisionK.getBranchNum() == 1 ? prevNodeK : prevNodeJ);

                                        fireNoNodeSelected();

                                        DecisionList shorterDecisionList = new DecisionList(decisionListK);
                                        shorterDecisionList.remove(shorterDecisionList.size() - 1);
                                        System.out.println("injecting if 2");

                                        MutableEndIfNode endIfNode = injectEndIf(decisionNode, endBranchNodes, BOD);
                                        alreadyProcessed.add(endIfNode);
                                        decisionListNodes.add(endIfNode);
                                        prevDecisionLists.add(shorterDecisionList);
                                        decistionLists.put(new Edge(endIfNode, BOD), shorterDecisionList);
                                        fireEndIfNodeAdded(endIfNode);
                                        //----
                                        fireUpdateDecisionLists(decistionLists);
                                        fireStep();
                                        removeExitPointFromPrevDlists(longerPrev, exitNode, new LinkedHashSet<>());
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
                nextDecisionList = new DecisionList();
            } else if (prevDecisionLists.size() == 1) {
                nextDecisionList = new DecisionList(prevDecisionLists.get(0));
            } else {
                DecisionList prefix = new DecisionList();
                Decision nextPismeno;
                int pocetVPrefixu = 0;
                looppocet:
                while (true) {
                    nextPismeno = null;
                    for (DecisionList decisionList : prevDecisionLists) {
                        if (decisionList.size() == pocetVPrefixu) {
                            break looppocet;
                        }

                        Decision currentDecision = decisionList.get(pocetVPrefixu);
                        if (nextPismeno == null) {
                            nextPismeno = currentDecision;
                        }
                        if (!currentDecision.getIfNode().equals(nextPismeno.getIfNode())) {
                            break looppocet;
                        }
                    }
                    prefix.add(nextPismeno);
                    pocetVPrefixu++;
                }
                for (int i = 0; i < prevDecisionLists.size(); i++) {
                    DecisionList decisionList = prevDecisionLists.get(i);
                    if (decisionList.size() > prefix.size()) {
                        rememberedDecisionLists.add(decisionList);
                        Edge gotoEdge = new Edge(decisionListNodes.get(i), BOD);
                        gotoEdges.add(gotoEdge);
                        fireEdgeMarked(gotoEdge, EdgeType.GOTO);
                    }
                    if (decisionList.size() > prefix.size() + 1) {
                        Node exitNode = decisionList.get(prefix.size() + 1).getIfNode();
                        removeExitPointFromPrevDlists(BOD, exitNode, new LinkedHashSet<>());
                    }
                }

                System.out.println("just merge of unstructured branches");
                for (Node prev : decisionListNodes) {
                    decistionLists.put(new Edge(prev, BOD), prefix);
                }
                fireStep();
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
                DecisionList mergedDecisionList = calculateDecisionListFromPrevNodes(BOD, prevNodes);
                alreadyProcessed.add(BOD);
                List<Node> nextNodes = getNextNodes(BOD);

                for (int branch = 0; branch < nextNodes.size(); branch++) {
                    Node next = nextNodes.get(branch);
                    Edge edge = new Edge(BOD, next);
                    DecisionList nextDecisionList = new DecisionList(mergedDecisionList);
                    if (nextNodes.size() > 1) {
                        nextDecisionList.add(new Decision(BOD, branch));
                    }
                    decistionLists.put(edge, DecisionList.unmodifiableList(nextDecisionList));
                    todoList.add(next);
                }
                fireNodeSelected(BOD);
                fireUpdateDecisionLists(decistionLists);
                fireStep();
            }
        } while (!todoList.isEmpty());
    }

    private MutableEndIfNode injectEndIf(Node decisionNode, List<Node> endBranchNodes, Node afterNode) {
        System.out.println("generated endif " + decisionNode);
        List<MutableNode> endBranchMutables = new ArrayList<>();
        if (!(afterNode instanceof MutableNode)) {
            return null;
        }
        MutableNode afterNodeMutable = (MutableNode) afterNode;

        for (Node prev : endBranchNodes) {
            if (prev instanceof MutableNode) {
                endBranchMutables.add((MutableNode) prev);
            } else {
                return null;
            }
        }

        int afterNodePrevIndex = Integer.MAX_VALUE;
        for (Node prev : endBranchMutables) {
            int index = afterNode.getPrev().indexOf(prev);
            if (index < afterNodePrevIndex) {
                afterNodePrevIndex = index;
            }
        }

        MutableEndIfNode endIfNode = endIfFactory.makeEndIfNode(decisionNode);

        System.out.println("afterNode prev size=" + afterNode.getPrev().size());

        //odstranit vazbu z prev->after
        for (int i = 0; i < endBranchMutables.size(); i++) {
            MutableNode prevMutable = endBranchMutables.get(i);
            prevMutable.removeNext(afterNode);
        }
        for (MutableNode prevMutable : endBranchMutables) {
            afterNodeMutable.removePrev(prevMutable);
        }

        //nastavit vazbu prev->endif 
        for (int i = 0; i < endBranchMutables.size(); i++) {
            MutableNode prevMutable = endBranchMutables.get(i);
            prevMutable.addNext(endIfNode);
            endIfNode.addPrev(prevMutable);
        }
        //nastavit vazbu endif->after
        endIfNode.addNext(afterNode);
        System.out.println("afterNodePrevIndex=" + afterNodePrevIndex);
        afterNodeMutable.addPrev(afterNodePrevIndex, endIfNode); //dat to na spravny index

        for (MutableNode prev : endBranchMutables) {
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

    private void fireUpdateDecisionLists(Map<Edge, DecisionList> decistionLists) {
        for (CodeStructureDetectorProgressListener l : listeners) {
            l.updateDecisionLists(decistionLists);
        }
    }

    private void fireNoNodeSelected() {
        for (CodeStructureDetectorProgressListener l : listeners) {
            l.noNodeSelected();
        }
    }

    private void fireMultiNodeJoined(MultiNode node) {
        for (CodeStructureDetectorProgressListener l : listeners) {
            l.multiNodeJoined(node);
        }
    }
}
