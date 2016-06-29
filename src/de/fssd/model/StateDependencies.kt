package de.fssd.model

/**
 * Created by Andre on 29.06.2016.
 */
interface StateDependencies {
    fun areVariableDependent(varID1: Int, varID2: Int):Boolean
}