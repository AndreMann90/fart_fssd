package de.fssd.model

import java.util.*

/**
 * Represents the Variable for a MC state
 */
class McVariable (val orderInQ: Int, val name: String) {
    val timeSeries = ArrayList<Float>();

    override fun toString(): String {
        return timeSeries.toString();
    }
}