package de.fssd.dataobjects;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Andre on 16.06.2016.
 */
public class FaultTreeNode {
    private @NotNull final String id;
    private @NotNull final String op;
    private @Nullable List<String> out;

    private @Nullable List<FaultTreeNode> outputs = null; // null means not computed yet
    private @Nullable List<FaultTreeNode> inputs = new LinkedList<>();

    public FaultTreeNode(String id, List<String> out, String op) {
        this.id = id;
        this.out = out;
        this.op = op;
    }

    void computeDependency(Map<String, FaultTreeNode> map) {
        if (outputs == null) {
            outputs = new ArrayList<>(out.size());
            for (String outID : out) {
                FaultTreeNode outNode = map.get(outID);
                outputs.add(outNode);
                outNode.inputs.add(this);
            }
            out = null; // No longer needed
        }
    }

    public @NotNull String getId() {
        return id;
    }

    public @Nullable List<FaultTreeNode> getOutputs() {
        return outputs;
    }

    public @Nullable List<FaultTreeNode> getInputs() {
        return outputs == null ? null : inputs;
    }

    public @NotNull String getOp() {
        return op;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FaultTreeNode)) return false;

        FaultTreeNode that = (FaultTreeNode) o;

        return getId().equals(that.getId());

    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
