package de.fssd.dataobjects;

import com.sun.istack.internal.NotNull;

import java.util.List;

public class FaultTree {
    private final @NotNull List<FaultTreeNode> nodes;
    private final @NotNull List<MCState> chain;
    private final int sampleCount;
    private final float missionTime;

    public FaultTree(List<FaultTreeNode> nodes, List<MCState> chain, int sampleCount, float missionTime) {
        this.nodes = nodes;
        this.chain = chain;
        this.sampleCount = sampleCount;
        this.missionTime = missionTime;
    }


    public List<FaultTreeNode> getNodes() {
        return nodes;
    }

    public List<MCState> getChain() {
        return chain;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public float getMissionTime() {
        return missionTime;
    }
}
