package de.fssd.model

import de.fssd.dataobjects.FaultTree
import de.fssd.dataobjects.MCState
import org.jgrapht.alg.ConnectivityInspector
import org.jgrapht.graph.*
import kotlin.system.measureTimeMillis

/**
 * Finds the independent components of a markov chain
 *
 * Useful for markov chain timeseries computation and required for proper variable order in [BDDBuilder], since the
 * evaluation need dependent variables to be grouped together
 */
object MCComponentFinder {

    fun compute(t: FaultTree): MutableList<Set<MCState>> {
        var components : MutableList<Set<MCState>> = mutableListOf()
        val time = measureTimeMillis {
            components = computeIntern(t)
        }
        System.err.println("Finding the components of MC took $time milliseconds")
        return components
    }

    private fun computeIntern(t: FaultTree): MutableList<Set<MCState>> {
        val m : MutableMap<String, MCState> = mutableMapOf()
        val g = SimpleGraph<MCState, DefaultEdge>(DefaultEdge::class.java)

        for (s in t.chain) {
            g.addVertex(s)
            m[s.id] = s
        }

        for (s in t.chain) {
            for (tr in s.transitions) {
                g.addEdge(s, m[tr.state])
            }
        }

        val sets = ConnectivityInspector<MCState, DefaultEdge>(g).connectedSets()
        val components : MutableList<Set<MCState>> = mutableListOf()
        for (set in sets) {
            val sorted : Set<MCState> = set.toSortedSet()
            components.add(sorted)
        }

        components.sortBy { el -> if(el.isEmpty()) "" else el.first().id }

        return components
    }
}
