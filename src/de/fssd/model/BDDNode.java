package de.fssd.model;

import jdd.bdd.BDD;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Andre on 19.06.2016.
 */
public class BDDNode {

    private final int nodeID;
    private final int varID;
    private final BDDNode highChild;
    private final BDDNode lowChild;
    private List<BDDNode> parent;

    /**
     * Use to wrap a {@link BDD} for a more programmer friendly NAVIGATION through the bdd AFTER having build it.
     * <p>
     *     IMPORTANT: DO NOT CHANGE the bdd since changes will not be detected.
     *                THE ONLY USE CASE for this class is a nice navigation through the final bdd.
     * </p>
     *
     * @param bdd the bdd
     * @param nodeID the root node
     */
    public BDDNode(BDD bdd, int nodeID) {
        this.nodeID = nodeID;
        this.varID = bdd.getVar(nodeID);

        this.parent = new LinkedList<>();

        if(hasChild()) {
            highChild = new BDDNode(bdd, bdd.getHigh(nodeID));
            highChild.parent.add(this);
            lowChild = new BDDNode(bdd, bdd.getLow(nodeID));
            lowChild.parent.add(this);
        } else {
            highChild = null;
            lowChild = null;
        }
    }

    public int getNodeID() {
        return nodeID;
    }

    public int getVarID() {
        return varID;
    }

    public BDDNode getHighChild() {
        return highChild;
    }

    public BDDNode getLowChild() {
        return lowChild;
    }

    public List<BDDNode> getParents() {
        return parent;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isOne() {
        return nodeID == 1;
    }

    public boolean isZero() {
        return nodeID == 0;
    }

    public boolean hasChild() {
        return !isOne() && !isZero();
    }
}
