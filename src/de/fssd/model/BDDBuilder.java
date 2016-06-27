package de.fssd.model;


import de.fssd.dataobjects.FaultTree;
import de.fssd.dataobjects.FaultTreeNode;
import de.fssd.dataobjects.MCState;
import de.fssd.parser.ParseException;
import jdd.bdd.BDD;

import java.util.*;

public class BDDBuilder {

    private Integer multiOp(BDD bdd, Vector<Integer> inputs, String op) {
        Integer prev = inputs.get(0);

        for (int idx = 1; idx < inputs.size(); idx++) {
            Integer c;
            switch (op) {
                case ">=1":
                    c = bdd.ref(bdd.or(prev, inputs.get(idx)));
                    break;
                case "&":
                    c = bdd.ref(bdd.and(prev, inputs.get(idx)));
                    break;
                default:
                    throw new ParseException("The operation " + op + " is not supported");
            }
            bdd.deref(prev);
            prev = c;
        }

        return prev;
    }

    private Map<String, FaultTreeNode> computeMap(List<FaultTreeNode> nodes) {
        Map<String, FaultTreeNode> map = new HashMap<>();
        for(FaultTreeNode node : nodes) {
            if(map.containsKey(node.getId())) {
                throw new ParseException("ID " + node.getId() + " is used twice");
            }
            map.put(node.getId(), node);
        }
        return map;
    }

    private void computeDependency(FaultTreeNode node, Map<String, FaultTreeNode> map) {
        for (String outID : node.getOut()) {
            FaultTreeNode outNode = map.get(outID);
            node.getOutputs().add(outNode);
            if (outNode == null) {
                throw new BDDBuildException("Invalid BDD specification, check your transitions");
            }
            outNode.getInputs().add(node);
        }
    }

    private void updateDependencies(FaultTree t) {
        Map<String, FaultTreeNode> map = computeMap(t.getNodes());
        for(FaultTreeNode node: t.getNodes()) {
            computeDependency(node, map);
        }
    }

    /**
     * Builds the BDD from the fault tree and returns the root node and the Markov
     * @param t the fault tree
     * @return the root node and Markov States
     */
    public BDDBuildResult build(FaultTree t) {
        /* Return bdd, top node, markov states */
        updateDependencies(t);

        BDD bdd = new BDD(1000);

        Map<MCState, Integer> stateToNodeIDMap = new HashMap<>();
        Map<Integer, MCState> varIDToStateMap = new HashMap<>();
        for (MCState s: t.getChain()) {
            int var = bdd.ref(bdd.createVar());
            stateToNodeIDMap.put(s, var);
            varIDToStateMap.put(bdd.getVar(var), s);
        }

        Map<FaultTreeNode, Integer> bddnodes = new HashMap<>();

        Integer top = 0;

        boolean changed = true;
        while (changed) {
            changed = false;
            for (FaultTreeNode n: t.getNodes()) {
                if (bddnodes.containsKey(n))
                    continue;
                Vector<Integer> inputs = new Vector<>();
                boolean usable = true;
                for (FaultTreeNode i : n.getInputs()) {
                    if (!bddnodes.containsKey(i)) {
                        usable = false;
                        break;
                    }
                    inputs.add(bddnodes.get(i));
                }
                for (MCState s : t.getChain()) {
                    if (s.getOut().contains(n.getId()))
                        inputs.add(stateToNodeIDMap.get(s));
                }

                if (usable) {
                    bddnodes.put(n, multiOp(bdd, inputs, n.getOp()));
                    changed = true;
                    top = bddnodes.get(n);
                }
            }
        }

        return new BDDBuildResult(new BDDNode(bdd, top), new Markov(t, varIDToStateMap), varIDToStateMap);
    }
}
