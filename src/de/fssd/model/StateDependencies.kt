package de.fssd.model

/**
 * Interface to be injected into [BDDNode]. (For Test Purpose)
 */
interface StateDependencies {
    fun areVariableDependent(varID1: Int, varID2: Int):Boolean
}