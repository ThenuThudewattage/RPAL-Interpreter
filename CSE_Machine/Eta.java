package CSE_Machine;

import PARSER.ASTNode;
import PARSER.ASTNodeType;

// Eta class representing an eta closure in the CSE machine
public class Eta extends ASTNode {
    // Delta object associated with the eta closure
    private Delta delta;

    // Constructor to initialize an Eta object
    public Eta() {
        setType(ASTNodeType.ETA);
    }

    // Method to get the value representation of the eta closure
    // Used if the program evaluation results in a partial application
    @Override
    public String getValue() {
        return "[eta closure: " + delta.getBoundVars().get(0) + ": " + delta.getIndex() + "]";
    }

    // Method to accept a NodeCopier visitor
    public Eta accept(NodeCopier nodeCopier) {
        return nodeCopier.getEtaCopy(this);
    }

    // Getter method for the Delta object associated with the eta closure
    public Delta getDelta() {
        return delta;
    }

    // Setter method for the Delta object associated with the eta closure
    public void setDelta(Delta delta) {
        this.delta = delta;
    }
}
