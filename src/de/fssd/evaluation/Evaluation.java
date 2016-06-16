package de.fssd.evaluation;

import de.fssd.model.Markov;
import jdd.bdd.BDD;

import java.util.stream.Stream;

/**
 * Created by Andre on 16.06.2016.
 */
public class Evaluation {

    public Evaluation (BDD bdd, int rootNote, Markov markov) {

    }

    private Stream<Float> consructFormular(BDD bdd) {
        /* USE: https://github.com/poetix/protonpack for zipping
         Streams are provided by Class Markov
        StreamUtils.zip(streamA,
                streamB,
                (a, b) -> a + b);*/
        return null;
    }
}
