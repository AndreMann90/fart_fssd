package de.fssd.model;

import de.fssd.dataobjects.FaultTree;
import de.fssd.dataobjects.FaultTreeNode;
import de.fssd.dataobjects.MCState;
import de.fssd.parser.ParseException;
import de.fssd.parser.Parser;
import jdd.bdd.BDD;
import jdd.util.Dot;

import java.io.File;
import java.util.*;

public class BDDBuilder {
    private Integer
    multiOp(BDD bdd, Vector<Integer> inputs, String op) {
        Integer prev = inputs.get(0);

        for (int idx = 1; idx < inputs.size(); idx++) {
            Integer c;
            switch (op) {
                case ">=1":
                    c = bdd.ref(bdd.or(prev, inputs.get(idx)));
                    break;
                default:
                    /* Raise an exception? */
                case "&":
                    c = bdd.ref(bdd.and(prev, inputs.get(idx)));
                    break;
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
            outNode.getInputs().add(node);
        }
    }

    private void updateDependencies(FaultTree t) {
        Map<String, FaultTreeNode> map = computeMap(t.getNodes());
        for(FaultTreeNode node: t.getNodes()) {
            computeDependency(node, map);
        }
    }

    public BDDBuilderResult build(FaultTree t) throws Exception {
        /* Return bdd, top node, markov states */
        updateDependencies(t);

        BDD bdd = new BDD(1000);

        Map<MCState, Integer> bddvars = new HashMap<>();
        for (MCState s: t.getChain()) {
            int var = bdd.ref(bdd.createVar());
            bddvars.put(s, var);
            System.out.println("Creating variable " + s.getId());
        }
        System.out.println("Done creating " + bddvars.size() + " vars");

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
                        inputs.add(bddvars.get(s));
                }

                if (usable) {
                    bddnodes.put(n, multiOp(bdd, inputs, n.getOp()));
                    changed = true;
                    top = bddnodes.get(n);

                    System.out.println("Node [" + top + "] " + n.getId() + ": " + inputs);
                }
            }
        }

        return new BDDBuilderResult(top, bdd);
    }
}
