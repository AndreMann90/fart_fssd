package de.fssd.model

import de.fssd.dataobjects.FaultTree
import de.fssd.dataobjects.MCState
import org.jgrapht.alg.ConnectivityInspector
import org.jgrapht.graph.*
import java.util.*

class MCComponentFinder {
    lateinit var sets: MutableList<Set<MCState>>
    constructor(t: FaultTree) {
        val m = HashMap<String, MCState>();
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

        sets = ConnectivityInspector<MCState, DefaultEdge>(g).connectedSets()
    }
}
