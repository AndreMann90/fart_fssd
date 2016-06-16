package de.fssd.dataobjects;

import com.sun.istack.internal.NotNull;

import java.util.List;

/**
 * Created by Andre on 16.06.2016.
 */
public class FaultTree {
    private final @NotNull List<FaultTreeNode> nodes;

    public FaultTree(List<FaultTreeNode> nodes) {
        this.nodes = nodes;
    }

    public List<FaultTreeNode> getNodes() {
        return nodes;
    }
}
