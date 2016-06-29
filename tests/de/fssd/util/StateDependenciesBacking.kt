package de.fssd.util

import de.fssd.model.StateDependencies

/**
 * Suitable for small groups of dependent states
 */
class StateDependenciesBacking : StateDependencies {

    private var dependencies : List<List<Int>>;

    constructor (dependencies: List<List<Int>>) {
        this.dependencies = dependencies
    }

    override fun areVariableDependent(varID1: Int, varID2: Int): Boolean {
        for (group in dependencies) {
            if(varID1 in group) {
                if(varID2 in group) {
                    return true
                } else {
                    return false
                }
            }
        }
        return false
    }

}