package de.fssd.dataobjects;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FaultTreeNode {
    private @NotNull final String id;
    private @NotNull final String op;

    private @Nullable List<String> out;

    /* These are initialized lazily */
    private @Nullable List<FaultTreeNode> outputs = null;
    private @Nullable List<FaultTreeNode> inputs = null;

    public FaultTreeNode(String id, List<String> out, String op) {
        this.id = id;
        this.out = out;
        this.op = op;
    }

    public List<String> getOut() { return out; }

    public @NotNull String getId() {
        return id;
    }

    public @Nullable List<FaultTreeNode> getOutputs() {
        if (outputs == null)
            outputs = new ArrayList<>();
        return outputs;
    }

    public @Nullable List<FaultTreeNode> getInputs() {
        if (inputs == null)
            inputs = new ArrayList<>();
        return inputs;
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
