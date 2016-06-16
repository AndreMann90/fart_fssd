package de.fssd.dataobjects;

import com.sun.istack.internal.NotNull;
import com.sun.org.apache.xpath.internal.operations.String;

import java.util.List;

/**
 * Created by Andre on 16.06.2016.
 */
public class MCState {
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

    public List<MCTransition> getTransitions() {
        return transitions;
    }
}
