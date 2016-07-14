package de.fssd.model

import jdd.bdd.BDD

/**
 * Use to wrap a [BDD] for a more programmer friendly NAVIGATION through the bdd AFTER having build it.
 *
 *
 * IMPORTANT: DO NOT CHANGE the bdd since changes will not be detected.
 * THE ONLY USE CASE for this class is a nice navigation through the <em>final</em> bdd.
 *
 * @param bdd the bdd
 * @param stateDependencies the stateDependencies
 * @param nodeID the root node
 */
class BDDNode (bdd: BDD, timeSeries: TimeSeries, stateDependencies: StateDependencies, val nodeID: Int) {
    val varID: Int
    val highChild: BDDNode?
    val lowChild: BDDNode?
    private val parent: MutableList<BDDNode>
    val probabilities: List<Float>
    /**
     * Returns true iff corresponding state of current node depends on state of *LOW** child node
     * @return true if s dependent to *LOW** child nodes
     ** */
    val isLowStateDependent: Boolean
    /**
     * Returns true iff corresponding state of current node depends on state of *HIGH** child node
     * @return true if s dependent to *HIGH** child nodes
     ** */
    val isHighStateDependent: Boolean

    init {
        this.varID = bdd.getVar(nodeID)
        this.parent = mutableListOf<BDDNode>()

        if (hasChild()) {
            this.probabilities = timeSeries.getProbabilitySeries(varID)!!
            this.highChild = BDDNode(bdd, timeSeries, stateDependencies, bdd.getHigh(nodeID))
            this.highChild.parent.add(this)
            this.isHighStateDependent = stateDependencies.areVariableDependent(this.varID, highChild.varID)
            this.lowChild = BDDNode(bdd, timeSeries, stateDependencies, bdd.getLow(nodeID))
            this.lowChild.parent.add(this)
            this.isLowStateDependent = if (highChild.varID == lowChild.varID) isHighStateDependent
                                       else stateDependencies.areVariableDependent(this.varID, lowChild.varID)
        } else {
            this.probabilities = listOf()
            this.highChild = null
            this.isHighStateDependent = false
            this.lowChild = null
            this.isLowStateDependent = false
        }
    }

    val parents: List<BDDNode>
        get() = parent

    val isRoot: Boolean
        get() = parent.isEmpty()

    val isOne: Boolean
        get() = nodeID == 1

    val isZero: Boolean
        get() = nodeID == 0

    fun hasChild(): Boolean {
        return !isOne && !isZero
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BDDNode) return false

        return varID == other.varID
                && (
                    (isOne && other.isOne)
                    ||
                    (isZero && other.isZero)
                    ||
                    (this.lowChild == other.lowChild && this.highChild == other.highChild)
                )
    }

    /**
     * Subtree of node for debugging
     * @return subtree
     */
    val treeString: String
        get() {
            if (isOne) {
                return "1"
            } else if (isZero) {
                return "0"
            } else {
                return "v" + varID + "(" + nodeID + "): [" + boolToSign(isLowStateDependent) +
                        lowChild!!.treeString + ", " + boolToSign(isHighStateDependent) +
                        highChild!!.treeString + "]"
            }
        }

    private fun boolToSign(b: Boolean): String {
        return if (b) "+" else "-"
    }

    override fun hashCode(): Int {
        return nodeID
    }

    override fun toString(): String {
        return "BDDNode{varID=$varID, nodeID=$nodeID}"
    }
}
