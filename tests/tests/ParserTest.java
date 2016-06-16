import de.fssd.dataobjects.FaultTree;
import de.fssd.parser.Parser;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by Andre on 16.06.2016.
 */
public class ParserTest {
    @org.junit.Test
    public void parse() throws Exception {
        Parser parser = new Parser();

        FaultTree faultTree = parser.parse(new File("testcases/test1.json"));
    }

}