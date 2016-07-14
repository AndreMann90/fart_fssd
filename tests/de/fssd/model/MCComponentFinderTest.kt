package de.fssd.model

import de.fssd.parser.Parser
import junit.framework.TestCase
import org.junit.Test
import java.io.File

class MCComponentFinderTest : TestCase() {
    @Test fun testComponentFinder() {
        val t = Parser.parse(File("testcases/HFTTestCase.json"))

        val components = MCComponentFinder.compute(t)

        assertEquals("Unexpected number of components", 2, components.size)
    }
}
