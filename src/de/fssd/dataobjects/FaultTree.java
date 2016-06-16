package de.fssd.dataobjects;

import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FaultTree {
    private final @NotNull List<FaultTreeNode> nodes = new ArrayList<>();
    private final @NotNull List<MCState> chain = new ArrayList<>();

    public List<FaultTreeNode> getNodes() {
        return nodes;
    }

    public List<MCState> getChain() {
        return chain;
    }
}
