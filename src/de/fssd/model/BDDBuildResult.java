package de.fssd.model;

import de.fssd.dataobjects.MCState;

import java.util.Map;

/**
 * Created by gbe on 6/23/16.
 */
public class BDDBuildResult {
    public BDDNode b;
    public Markov m;
    public Map<Integer, MCState> stateMap;

    public BDDBuildResult(BDDNode b, Markov m, Map<Integer, MCState> stateMap) {
        this.b = b;
        this.m = m;
        this.stateMap = stateMap;
    }
}
