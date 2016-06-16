package de.fssd.dataobjects;

import com.sun.istack.internal.NotNull;

import java.util.List;

/**
 * Created by Andre on 16.06.2016.
 */
public class FaultTree {
    private final @NotNull List<FaultTreeNode> nodes;
    private final @NotNull List<MCState> chain;

    public FaultTree(List<FaultTreeNode> nodes, List<MCState> chain) {
        this.nodes = nodes;
        this.chain = chain;
    }

    public List<FaultTreeNode> getNodes() {
        return nodes;
    }

    public List<MCState> getChain() {
        return chain;
    }
}
