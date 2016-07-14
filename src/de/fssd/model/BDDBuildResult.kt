package de.fssd.model

import de.fssd.dataobjects.MCState
import jdd.bdd.BDD

/**
 * The result from [BDDBuilder]
 */
data class BDDBuildResult(val rootNodes: List<BDDNode>, val bdd: BDD,
                          val markov: Markov, val stateMap: Map<Int, MCState>)