package de.fssd.dataobjects

import com.sun.istack.internal.NotNull

class MCState(val id: String, val p0: Float, val out: List<String>, val transitions: List<MCTransition>) : Comparable<MCState> {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val mcState = o as MCState?

        return id == mcState!!.id

    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "MCState{" +
                "id='" + id + '\'' +
                ", p0=" + p0 +
                ", out=" + out +
                '}'
    }

    override fun compareTo(other: MCState): Int {
        return id.compareTo(other.id)
    }
}
