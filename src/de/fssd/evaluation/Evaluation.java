package de.fssd.evaluation;

import com.codepoetics.protonpack.functions.TriFunction;
import com.sun.istack.internal.Nullable;
import de.fssd.model.BDDNode;
import de.fssd.model.Markov;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Andre on 16.06.2016.
 */
public class Evaluation {

    private static Supplier<Float> ones = () -> 1f;

    private Markov markov;

    public Evaluation (Markov markov) {
        this.markov = markov;
    }

    public List<Float> evaluateWithRootNode(BDDNode rootNode) {
        if(!rootNode.isRoot()) {
            throw new AssertionError("Not the root node");
        } else if(rootNode.isOne()) {
            return Collections.nCopies(markov.getTimeseriesCount(), 1f);
        }
        Stream<Float> result = constructFormulaTopDown(rootNode);
        if(result != null) {
            return result.collect(Collectors.toList());
        } else {
            return new LinkedList<>();
        }
    }


    /**
     * Constructs the formula top down.
     * @param node the root node
     * @return formula encoded in stream, null if coming from zero node
     */
    private @Nullable Stream<Float> constructFormulaTopDown(BDDNode node) {
        if(node.isZero()) {
            return null;
        } else if(node.isOne()) {
            return Stream.generate(ones);
        } else {
            final Stream<Float> current = markov.getProbabilitySeries(node.getVarID());

            final Stream<Float> high = constructFormulaTopDown(node.getHighChild());
            final Stream<Float> low = constructFormulaTopDown(node.getLowChild());

            if(low == null && high == null) {
                return null;
            } else {
                final Stream<Float> result;

                if(high == null) {
                    result = zip(current, low, (a, b) -> (1-a) * b);
                } else if(low == null) {
                    result = zip(current, high, (a, b) -> a * b);
                } else {
                    result = zip(current, low, high, (a, b, c) -> (1-a)*b + a*c);
                }

                return result;
            }
        }
    }

/* TODO revise
    public List<Float> evaluateWithOneNode(BDDNode oneNode) {
        if(!oneNode.isOne()) {
            throw new AssertionError("Not one node");
        } else if(oneNode.isRoot()) {
            return Collections.nCopies(markov.getTimeseriesCount(), 1f);
        } else {
            return constructFormulaBottomUp(oneNode).collect(Collectors.toList());
        }
    }
*/
    /**
     * Constructs the formula bottom up.
     * @param node the one node
     * @return formula encoded in stream
     */
    private Stream<Float> constructFormulaBottomUp(BDDNode node) {
        //TODO revise
        final Stream<Float> current;
        if(node.isOne()) {
            current = Stream.generate(ones);
        } else if(node.isZero()) {
            throw new AssertionError("Zero node is impossible in a proper built bdd (starting at one and recursively " +
                    "accessing the parents)");
        } else {
            current = markov.getProbabilitySeries(node.getVarID());
        }
        if(node.isRoot()) {
            return current;
        } else {
            Iterator<BDDNode> parents = node.getParents().iterator();
            Stream<Float> parentStream = constructFormulaBottomUp(parents.next());
            while (parents.hasNext()) {
                Stream<Float> ps = constructFormulaBottomUp(parents.next());
                parentStream = zip(parentStream, ps, (a, b) -> a + b);
            }
            return zip(current, parentStream, (a, b) -> a * b);
        }
    }


    private Stream<Float> zip(Stream<Float> aStream, Stream<Float> bStream, BinaryOperator<Float> op) {
        /* USE: https://github.com/poetix/protonpack for zipping
         Streams are provided by Class Markov
        StreamUtils.zip(streamA,
                streamB,
                (a, b) -> a + b);*/
        return Stream.generate(ones); //TODO
    }
    private Stream<Float> zip(Stream<Float> aStream, Stream<Float> bStream, Stream<Float> cStream, TriFunction<Float, Float, Float, Float> op) {
        /* USE: https://github.com/poetix/protonpack for zipping
         Streams are provided by Class Markov
        StreamUtils.zip(streamA,
                streamB,
                (a, b) -> a + b);*/
        return Stream.generate(ones); //TODO
    }
}
