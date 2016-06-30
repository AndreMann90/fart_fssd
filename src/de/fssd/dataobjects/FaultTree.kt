package de.fssd.dataobjects

data class FaultTree(val nodes: List<FaultTreeNode>, val chain: List<MCState>, var sampleCount: Int, var missionTime: Float)