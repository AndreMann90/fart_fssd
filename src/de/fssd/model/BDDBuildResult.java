package de.fssd.model;

import de.fssd.dataobjects.MCState;

import java.util.Map;

/**
 * Created by gbe on 6/23/16.
 */
public class BDDBuildResult {
    public BDDNode rootNode;
    public Markov markov;
    public Map<Integer, MCState> stateMap;

    public BDDBuildResult(BDDNode b, Markov markov, Map<Integer, MCState> stateMap) {
        this.rootNode = b;
        this.markov = markov;
        this.stateMap = stateMap;
    }
}
