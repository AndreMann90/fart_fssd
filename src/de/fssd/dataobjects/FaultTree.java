package de.fssd.dataobjects;

import com.sun.istack.internal.NotNull;
import de.fssd.parser.ParseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Andre on 16.06.2016.
 */
public class FaultTree {
    private final @NotNull List<FaultTreeNode> nodes;
    private final @NotNull List<MCState> chain;

    public FaultTree(List<FaultTreeNode> nodes, List<MCState> chain) {
        Map<String, FaultTreeNode> map = computeMap(nodes);
        for(FaultTreeNode node : nodes) {
            node.computeDependency(map);
        }
        this.nodes = nodes;
        this.chain = chain;
    }

    private Map<String, FaultTreeNode> computeMap(List<FaultTreeNode> nodes) {
        Map<String, FaultTreeNode> map = new HashMap<>();
        for(FaultTreeNode node : nodes) {
            if(map.containsKey(node.getId())) {
                throw new ParseException("ID " + node.getId() + " is used twice");
            }
            map.put(node.getId(), node);
        }
        return map;
    }

    public List<FaultTreeNode> getNodes() {
        return nodes;
    }

    public List<MCState> getChain() {
        return chain;
    }
}
