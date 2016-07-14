package de.fssd.model

import de.fssd.dataobjects.FaultTree
import de.fssd.dataobjects.MCState
import org.jgrapht.alg.ConnectivityInspector
import org.jgrapht.graph.*
import java.util.*

class MCComponentFinder {
    val components: MutableList<Set<MCState>>

    constructor(t: FaultTree) {
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
        components = mutableListOf()
        for (set in sets) {
            val sorted : Set<MCState> = set.toSortedSet()
            components.add(sorted)
        }

        components.sortBy { el -> if(el.isEmpty()) "" else el.first().id }
    }
}
