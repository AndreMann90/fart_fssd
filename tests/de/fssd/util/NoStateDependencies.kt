package de.fssd.util

import de.fssd.model.StateDependencies

/**
 * Created by Andre on 29.06.2016.
 */
object NoStateDependencies : StateDependencies {
    override fun areVariableDependent(varID1: Int, varID2: Int): Boolean {
        return false
    }
}