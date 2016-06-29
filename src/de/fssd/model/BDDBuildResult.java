package de.fssd.model;

import de.fssd.dataobjects.MCState;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by gbe on 6/23/16.
 */
public class BDDBuildResult {
    public ArrayList<BDDNode> rootNodes;
    public Markov markov;
    public Map<Integer, MCState> stateMap;

    public BDDBuildResult(ArrayList<BDDNode> b, Markov markov, Map<Integer, MCState> stateMap) {
        this.rootNodes = b;
        this.markov = markov;
        this.stateMap = stateMap;
    }
}
