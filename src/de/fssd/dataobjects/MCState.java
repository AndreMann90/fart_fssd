package de.fssd.dataobjects;

import com.sun.istack.internal.NotNull;

import java.util.List;

public class MCState implements Comparable {
    private final @NotNull String id;
    private final float p0;
    private final @NotNull List<String> out;
    private final @NotNull List<MCTransition> transitions;

    public MCState(String id, float p0, List<String> out, List<MCTransition> transitions) {
        this.id = id;
        this.p0 = p0;
        this.out = out;
        this.transitions = transitions;
    }

    public String getId() {
        return id;
    }

    public List<String> getOut() {
        return out;
    }

    public float getP0() {
        return p0;
    }

    public List<MCTransition> getTransitions() {
        return transitions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MCState mcState = (MCState) o;

        return getId().equals(mcState.getId());

    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return "MCState{" +
                "id='" + id + '\'' +
                ", p0=" + p0 +
                ", out=" + out +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof MCState) {
            return this.id.compareTo(((MCState) o).id);
        } else {
            return 0;
        }
    }
}
