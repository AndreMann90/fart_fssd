package de.fssd.dataobjects;

import com.sun.istack.internal.NotNull;

import java.util.List;

/**
 * Created by Andre on 16.06.2016.
 */
public class FaultTreeNode {
    private @NotNull final String id;
    private @NotNull final List<String> out;
    private @NotNull final String op;

    public FaultTreeNode(String id, List<String> out, String op) {
        this.id = id;
        this.out = out;
        this.op = op;
    }

    public @NotNull String getId() {
        return id;
    }

    public @NotNull List<String> getOut() {
        return out;
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
