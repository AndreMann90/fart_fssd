package de.fssd.dataobjects

class MCState(val id: String, val p0: Float, val out: List<String>, val transitions: List<MCTransition>) : Comparable<MCState> {



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

    override fun equals(other: Any?): Boolean{
        if (this === other) return true
        if (other !is MCState) return false

        if (id != other.id) return false

        return true
    }
}
