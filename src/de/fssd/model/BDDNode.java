package de.fssd.model;

import com.sun.istack.internal.NotNull;
import jdd.bdd.BDD;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Andre on 19.06.2016.
 */
public class BDDNode {
    private final BDD bdd;
    private final int nodeID;
    private final int varID;
    private final BDDNode highChild;
    private final BDDNode lowChild;
    private @NotNull List<BDDNode> parent;

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
        this.bdd = bdd;
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

    public BDD getBDD() { return bdd; }

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
        return parent.isEmpty();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BDDNode)) return false;

        BDDNode bddNode = (BDDNode) o;

        return getVarID() == bddNode.getVarID()
                &&
                (
                        (isOne() && bddNode.isOne())
                        ||
                        (isZero() && bddNode.isZero())
                        ||
                        (this.lowChild.equals(bddNode.lowChild) && this.highChild.equals(bddNode.highChild))
                );
    }

    /**
     * Subtree of node for debugging
     * @return subtree
     */
    public String getTreeString() {
        if(isOne()) {
            return "1";
        } else if (isZero()) {
            return "0";
        } else {
            return "v" + getVarID() + "(" + getNodeID() + "): [" + getLowChild().getTreeString() + ", " +getHighChild().getTreeString() + "]";
        }
    }

    @Override
    public int hashCode() {
        return getNodeID();
    }

    @Override
    public String toString() {
        return "BDDNode{" +
                "varID=" + varID +
                ", nodeID=" + nodeID +
                '}';
    }
}
