package de.fssd.model

import de.fssd.parser.Parser
import junit.framework.TestCase
import org.junit.Test
import java.io.File

class MCComponentFinderTest : TestCase() {
    @Test fun testComponentFinder() {
        val p = Parser()
        val t = p.parse(File("testcases/HFTTestCase.json"))

        val f = MCComponentFinder(t)

        assertEquals("Unexpected number of components", 2, f.components.size)
    }
}
