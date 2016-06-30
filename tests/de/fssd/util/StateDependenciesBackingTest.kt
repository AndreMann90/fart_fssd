package de.fssd.util

import org.junit.Test

import org.junit.Assert.*

/**
 * Created by Andre on 30.06.2016.
 */
class StateDependenciesBackingTest {
    @Test
    fun areVariableDependent() {
        var groupA = listOf(1,2,3)
        var groupB = listOf(4,5)
        var dependencies = StateDependenciesBacking(listOf(groupA, groupB))

        assertTrue(dependencies.areVariableDependent(1,2));
        assertTrue(dependencies.areVariableDependent(3,2));
        assertTrue(dependencies.areVariableDependent(1,3));

        assertTrue(dependencies.areVariableDependent(4,5));
        assertTrue(dependencies.areVariableDependent(5,4));

        assertFalse(dependencies.areVariableDependent(1,4));
        assertFalse(dependencies.areVariableDependent(4,2));
        assertFalse(dependencies.areVariableDependent(3,5));
    }
}