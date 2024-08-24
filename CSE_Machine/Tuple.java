package CSE_Machine;

import PARSER.ASTNode;
import PARSER.ASTNodeType;

// Tuple class represents a tuple node in the abstract syntax tree (AST)
public class Tuple extends ASTNode {

    // Constructor to initialize the type of the tuple node
    public Tuple() {
        setType(ASTNodeType.TUPLE);
    }

    // Method to get the string representation of the tuple's value
    @Override
    public String getValue() {
        ASTNode childNode = getChild();
        if (childNode == null)
            return "nil";

        String printValue = "(";
        while (childNode.getSibling() != null) {
            printValue += childNode.getValue() + ", ";
            childNode = childNode.getSibling();
        }
        printValue += childNode.getValue() + ")";
        return printValue;
    }

    // Method to accept a NodeCopier visitor and return a copy of the tuple
    public Tuple accept(NodeCopier nodeCopier) {
        return nodeCopier.getTupleCopy(this);
    }
}
