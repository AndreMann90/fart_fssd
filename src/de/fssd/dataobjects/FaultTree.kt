package de.fssd.dataobjects

data class FaultTree(val nodes: List<FaultTreeNode>, val chain: List<MCState>, var sampleCount: Int, var missionTime: Float) {
    val sampleTime : Float
        get() = if (sampleCount > 1) missionTime / (sampleCount - 1) else 0f
}