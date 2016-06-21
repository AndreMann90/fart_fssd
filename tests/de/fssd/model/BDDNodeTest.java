package de.fssd.model;

import org.junit.Before;
import org.junit.Test;
import de.fssd.util.TestFactory;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by Andre on 21.06.2016.
 */
public class BDDNodeTest {

    private BDDNode rootNode;

    @Before
    public void setup() {
        rootNode = TestFactory.getRAIDTest().getKey();
        System.out.println(rootNode.getTreeString());
    }

    @Test
    public void navigation() throws Exception {
        assertEquals(rootNode.getNodeID(), rootNode.getLowChild().getParents().get(0).getNodeID());
        assertTrue(rootNode.getParents().isEmpty());
        assertTrue(rootNode.getHighChild().getHighChild().getLowChild() == null);
    }

    @Test
    public void childAndRootChecks() throws Exception {
        assertTrue(rootNode.isRoot());
        assertFalse(rootNode.isOne());
        assertFalse(rootNode.isZero());

        BDDNode low = rootNode.getLowChild();
        assertFalse(low.isRoot());
        assertFalse(low.isOne());
        assertFalse(low.isZero());

        assertTrue(low.getLowChild().isZero());
        assertFalse(low.getLowChild().isOne());
        assertFalse(low.getLowChild().isRoot());

        assertTrue(low.getHighChild().isOne());
        assertFalse(low.getHighChild().isZero());
        assertFalse(low.getHighChild().isRoot());
    }

}