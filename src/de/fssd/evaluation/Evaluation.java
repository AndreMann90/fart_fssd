package de.fssd.evaluation;

import com.codepoetics.protonpack.StreamUtils;
import com.codepoetics.protonpack.functions.TriFunction;
import com.sun.istack.internal.NotNull;
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
    private static Supplier<Float> zeros = () -> 0f;

    private TimeSeries timeSeries;
    private Map<BDDNode, List<Float>> computedTable;

    public Evaluation (TimeSeries timeSeries) {
        this.timeSeries = timeSeries;
    }


    public List<Float> evaluateWithRootNodeAndComputedTable(BDDNode rootNode) {
        if(!rootNode.isRoot()) {
            throw new AssertionError("Not the root node");
        } else if(rootNode.isOne()) {
            return Collections.nCopies(timeSeries.getSamplePointsCount(), 1f);
        } else if(rootNode.isZero()) {
            return Collections.nCopies(timeSeries.getSamplePointsCount(), 0f);
        }
        computedTable = new HashMap<>();
        return constructFormulaTopDownWithComputedTable(rootNode).collect(Collectors.toList());
    }

    /**
     * Constructs the formula top down using a comuted table
     * @param node the root node
     * @return formula encoded in stream, null if coming from zero node
     */
    private @NotNull Stream<Float> constructFormulaTopDownWithComputedTable(BDDNode node) {
        if(node.isZero()) {
            return Stream.generate(zeros);
        } else if(node.isOne()) {
            return Stream.generate(ones);
        } else if(computedTable.containsKey(node)) {
            return computedTable.get(node).stream();
        } else {
            List<Float> result = formulaFromChildes(node).collect(Collectors.toList());
            computedTable.put(node, result);
            return result.stream();
        }
    }

    private @NotNull Stream<Float> formulaFromChildes(BDDNode node) {
        final Stream<Float> current = timeSeries.getProbabilitySeries(node.getVarID());

        // variable names correspond to formula in "BDD Evaluation with Restricted Variables" (page 2 at top)
        final Stream<Float> g2 = constructFormulaTopDownWithComputedTable(node.getLowChild());
        final Stream<Float> g1_x1 = getHigh(node);

        // formula in zip correspond to formula in "BDD Evaluation with Restricted Variables" (page 2 at top)
        if(node.isLowStateDependent()) {
            final Stream<Float> h2_x1 = getDependentLow(node);
            Stream<Float> diff = zip(g1_x1, h2_x1, (G1, H2) -> G1 - H2);
            return zip(current, g2, diff, (X, G2, Diff) -> G2 + X * Diff);
        } else {
            return zip(current, g2, g1_x1, (X, G2, G1) -> G2 + X * (G1 - G2));
        }
    }

    private @NotNull Stream<Float> getHigh(BDDNode node) {
        if (node.isHighStateDependent()) {
            BDDNode high = node.getHighChild();
            while (high.isLowStateDependent() && high.hasChild()) {
                high = high.getLowChild();
            }
            return constructFormulaTopDownWithComputedTable(high.getLowChild());
        } else {
            return constructFormulaTopDownWithComputedTable(node.getHighChild());
        }
    }

    private @NotNull Stream<Float> getDependentLow(BDDNode node) {
        assert node.isLowStateDependent();
        BDDNode low = node.getLowChild();
        while (low.isLowStateDependent() && low.hasChild()) {
            low = low.getLowChild();
        }
        return constructFormulaTopDownWithComputedTable(low.getLowChild());
    }

    private Stream<Float> zip(Stream<Float> aStream, Stream<Float> bStream, BiFunction<Float, Float, Float> op) {
        return StreamUtils.zip(aStream, bStream, op);
    }

    private Stream<Float> zip(Stream<Float> aStream, Stream<Float> bStream, Stream<Float> cStream, TriFunction<Float, Float, Float, Float> op) {
        return StreamUtils.zip(aStream, bStream, cStream, op);
    }
}
