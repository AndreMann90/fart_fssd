package de.fssd.evaluation

import de.fssd.model.BDDNode
import de.fssd.model.TimeSeries
import java.util.*
import kotlin.system.measureTimeMillis

/**
 * Created by Andre on 30.06.2016.
 */
class Evaluation {
    private val ones = { 1f }
    private val zeros = { 0f }

    private val timeSeries: TimeSeries
    lateinit var computedTable: MutableMap<BDDNode, List<Float>>

    constructor(timeSeries: TimeSeries) {
        this.timeSeries = timeSeries
    }

    fun evaluateMultipleRootNodes(rootNodes: List<BDDNode>): Map<BDDNode, List<Float>> {
        val results : MutableMap<BDDNode, List<Float>> = mutableMapOf()
        computedTable = HashMap<BDDNode, List<Float>>()
        for (rootNode in rootNodes) {
            results.put(rootNode, evaluateWithOneRootNode(rootNode))
        }
        return results
    }

    fun evaluateWithRootNodeAndComputedTable(rootNode: BDDNode): List<Float> {
        computedTable = HashMap<BDDNode, List<Float>>()
        return evaluateWithOneRootNode(rootNode)
    }

    private fun evaluateWithOneRootNode(rootNode: BDDNode): List<Float> {
        if (!rootNode.isRoot) {
            throw AssertionError("Not the root node")
        } else if (rootNode.isOne) {
            return Collections.nCopies(timeSeries.samplePointsCount, 1f)
        } else if (rootNode.isZero) {
            return Collections.nCopies(timeSeries.samplePointsCount, 0f)
        }
        return constructFormulaTopDownWithComputedTable(rootNode).toList()
    }

    /**
     * Constructs the formula top down using a computed table
     * @param node the root node
     * *
     * @return formula encoded in a sequence
     */
    private fun constructFormulaTopDownWithComputedTable(node: BDDNode?): Sequence<Float> {
        if (node == null || node.isZero) {
            return generateSequence(zeros)
        } else if (node.isOne) {
            return generateSequence(ones)
        } else if (computedTable.containsKey(node)) {
            return computedTable[node]!!.asSequence()
        } else {
            val result = formulaFromChildes(node).toList()
            computedTable.put(node, result)
            return result.asSequence() // Eleganz vom Ansatz weg...
        }
    }

    private fun formulaFromChildes(node: BDDNode): Sequence<Float> {
        // conforms to "BDD Evaluation with Restricted Variables.pdf"
        val current = node.probabilities.asSequence()
        val g2 = constructFormulaTopDownWithComputedTable(node.lowChild)
        val g1_x1 = getHigh(node)

        if (node.isLowStateDependent) {
            val h2_x1 = getIndependentLow(node)
            return zip(g1_x1, g2, h2_x1, current) {g1, g2, h2, x -> g2 + x * (g1 - h2)}
        } else {
            return zip(g1_x1, g2, current) {g1, g2, x -> g2 + x * (g1 - g2)}
        }
    }

    private fun getHigh(node: BDDNode): Sequence<Float> {
        if (node.isHighStateDependent) {
            var high = node.highChild!!
            while (high.isLowStateDependent && high.hasChild()) {
                high = high.lowChild!!
            }
            return constructFormulaTopDownWithComputedTable(high.lowChild)
        } else {
            return constructFormulaTopDownWithComputedTable(node.highChild)
        }
    }

    private fun getIndependentLow(node: BDDNode): Sequence<Float> {
        assert(node.isLowStateDependent)
        var low = node.lowChild!!
        while (low.isLowStateDependent && low.hasChild()) {
            low = low.lowChild!!
        }
        return constructFormulaTopDownWithComputedTable(low.lowChild)
    }

    private fun <T> zip(a: Sequence<T>, b : Sequence<T>, c: Sequence<T>, op: (T, T, T) -> T) : Sequence<T> {
        return MergingSequence3(a, b, c, op)
    }

    private fun <T> zip(a: Sequence<T>, b : Sequence<T>, c: Sequence<T>, d: Sequence<T>, op: (T, T, T, T) -> T) : Sequence<T> {
        return MergingSequence4(a, b, c, d, op)
    }
}

// adapted from std lib:
internal class MergingSequence3<T1, T2, T3, V>
constructor(private val sequence1: Sequence<T1>,
            private val sequence2: Sequence<T2>,
            private val sequence3: Sequence<T3>,
            private val transform: (T1, T2, T3) -> V) : Sequence<V> {
    override fun iterator(): Iterator<V> = object : Iterator<V> {
        val iterator1 = sequence1.iterator()
        val iterator2 = sequence2.iterator()
        val iterator3 = sequence3.iterator()
        override fun next(): V {
            return transform(iterator1.next(), iterator2.next(), iterator3.next())
        }

        override fun hasNext(): Boolean {
            return iterator1.hasNext() && iterator2.hasNext() && iterator3.hasNext()
        }
    }
}

internal class MergingSequence4<T1, T2, T3, T4, V>
constructor(private val sequence1: Sequence<T1>,
            private val sequence2: Sequence<T2>,
            private val sequence3: Sequence<T3>,
            private val sequence4: Sequence<T4>,
            private val transform: (T1, T2, T3, T4) -> V) : Sequence<V> {
    override fun iterator(): Iterator<V> = object : Iterator<V> {
        val iterator1 = sequence1.iterator()
        val iterator2 = sequence2.iterator()
        val iterator3 = sequence3.iterator()
        val iterator4 = sequence4.iterator()
        override fun next(): V {
            return transform(iterator1.next(), iterator2.next(), iterator3.next(), iterator4.next())
        }

        override fun hasNext(): Boolean {
            return iterator1.hasNext() && iterator2.hasNext() && iterator3.hasNext() && iterator4.hasNext()
        }
    }
}