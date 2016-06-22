package de.fssd.evaluation;

import com.codepoetics.protonpack.StreamUtils;
import com.codepoetics.protonpack.functions.TriFunction;
import com.sun.istack.internal.Nullable;
import de.fssd.model.BDDNode;
import de.fssd.model.TimeSeries;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Andre on 16.06.2016.
 */
public class Evaluation {

    private static Supplier<Float> ones = () -> 1f;

    private TimeSeries timeSeries;
    private Map<BDDNode, Stream<Float>> computedTable;

    public Evaluation (TimeSeries timeSeries) {
        this.timeSeries = timeSeries;
    }

    public List<Float> evaluateWithRootNode(BDDNode rootNode) {
        if(!rootNode.isRoot()) {
            throw new AssertionError("Not the root node");
        } else if(rootNode.isOne()) {
            return Collections.nCopies(timeSeries.getTimeseriesCount(), 1f);
        }
        Stream<Float> result = constructFormulaTopDown(rootNode);
        if(result != null) {
            return result.collect(Collectors.toList());
        } else {
            return Collections.nCopies(timeSeries.getTimeseriesCount(), 0f);
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
            return constructFormulaFromChild(node);
        }
    }

    public List<Float> evaluateWithRootNodeAndComputedTable(BDDNode rootNode) {
        if(!rootNode.isRoot()) {
            throw new AssertionError("Not the root node");
        } else if(rootNode.isOne()) {
            return Collections.nCopies(timeSeries.getTimeseriesCount(), 1f);
        }
        computedTable = new HashMap<>();
        Stream<Float> result = constructFormulaTopDownWithComputedTable(rootNode);
        if(result != null) {
            return result.collect(Collectors.toList());
        } else {
            return Collections.nCopies(timeSeries.getTimeseriesCount(), 0f);
        }
    }

    /**
     * Constructs the formula top down using a comuted table
     * @param node the root node
     * @return formula encoded in stream, null if coming from zero node
     */
    private @Nullable Stream<Float> constructFormulaTopDownWithComputedTable(BDDNode node) {
        if(node.isZero()) {
            return null;
        } else if(node.isOne()) {
            return Stream.generate(ones);
        } else if(computedTable.containsKey(node)) {
            return computedTable.get(node);
        } else {
            return constructFormulaFromChild(node);
        }
    }

    private @Nullable Stream<Float> constructFormulaFromChild(BDDNode node) {
        final Stream<Float> current = timeSeries.getProbabilitySeries(node.getVarID());

        final Stream<Float> low = constructFormulaTopDown(node.getLowChild());
        final Stream<Float> high = constructFormulaTopDown(node.getHighChild());

        if(low == null && high == null) {
            return null;
        } else {
            final Stream<Float> result;

            if(high == null) {
                result = zip(current, low, (c, l) -> (1-l) * c);
            } else if(low == null) {
                result = zip(current, high, (c, h) -> c * h);
            } else {
                result = zip(current, low, high, (c, l, h) -> ((1-c) * l) + (c * h));
            }

            return result;
        }
    }

/* TODO revise
    public List<Float> evaluateWithOneNode(BDDNode oneNode) {
        if(!oneNode.isOne()) {
            throw new AssertionError("Not one node");
        } else if(oneNode.isRoot()) {
            return Collections.nCopies(timeSeries.getTimeseriesCount(), 0f);
        } else {
            return constructFormulaBottomUp(oneNode).collect(Collectors.toList());
        }
    }

    /**
     * Constructs the formula bottom up.
     * @param node the one node
     * @return formula encoded in stream
     *
    private Stream<Float> constructFormulaBottomUp(BDDNode node) {
        final Stream<Float> current;
        if(node.isOne()) {
            current = Stream.generate(ones);
        } else if(node.isZero()) {
            throw new AssertionError("Zero node is impossible in a proper built bdd (starting at one and recursively " +
                    "accessing the parents)");
        } else {
            current = timeSeries.getProbabilitySeries(node.getVarID());
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
*/

    private Stream<Float> zip(Stream<Float> aStream, Stream<Float> bStream, BiFunction<Float, Float, Float> op) {
        return StreamUtils.zip(aStream, bStream, op);
    }

    private Stream<Float> zip(Stream<Float> aStream, Stream<Float> bStream, Stream<Float> cStream, TriFunction<Float, Float, Float, Float> op) {
        return StreamUtils.zip(aStream, bStream, cStream, op);
    }
}
