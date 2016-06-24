package de.fssd.integrationtest;

        import de.fssd.dataobjects.FaultTree;
        import de.fssd.evaluation.Evaluation;
        import de.fssd.model.BDDBuildResult;
        import de.fssd.model.BDDBuilder;
        import de.fssd.parser.Parser;
        import de.fssd.util.TimeSeriesFromCSV;

        import java.io.File;
        import java.util.List;

        import static org.junit.Assert.assertEquals;

public class IntegrationTest {

    @org.junit.Test
    public void integrationTest() throws Exception {
        Parser parser = new Parser();
        FaultTree faultTree = parser.parse(new File("testcases/HFTTestCase.json"));
        BDDBuildResult result = new BDDBuilder().build(faultTree);
        Evaluation evaluation = new Evaluation(result.markov);
        List<Float> topEventSeries = evaluation.evaluateWithRootNodeAndComputedTable(result.rootNode);

        List<Float> expectedTopEventSeries = new TimeSeriesFromCSV(new File("testcases/TestCaseForProgrammingProject.csv"))
                .getRemainingResultPerID("g16");

        assertEquals(expectedTopEventSeries, topEventSeries);
    }
}