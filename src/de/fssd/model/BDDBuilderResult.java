package de.fssd.model;

import jdd.bdd.BDD;

public class BDDBuilderResult {
    public Integer top;
    public BDD bdd;

    public BDDBuilderResult(Integer top, BDD bdd) {
        this.top = top;
        this.bdd = bdd;
    }
}
