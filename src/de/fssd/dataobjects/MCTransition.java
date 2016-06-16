package de.fssd.dataobjects;

import com.sun.org.apache.xpath.internal.operations.String;

/**
 * Created by Andre on 16.06.2016.
 */
public class MCTransition {
    private final float p;
    private final String state;

    public MCTransition(float p, String state) {
        this.p = p;
        this.state = state;
    }

    public float getP() {
        return p;
    }

    public String getState() {
        return state;
    }
}
