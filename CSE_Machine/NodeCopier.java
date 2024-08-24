package CSE_Machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import PARSER.ASTNode;

// NodeCopier class responsible for creating deep copies of ASTNode objects
public class NodeCopier {

    // Method to create a deep copy of an ASTNode object
    public ASTNode copy(ASTNode astNode) {
        ASTNode copy = new ASTNode();
        if (astNode.getChild() != null)
            copy.setChild(astNode.getChild().accept(this));
        if (astNode.getSibling() != null)
            copy.setSibling(astNode.getSibling().accept(this));
        copy.setType(astNode.getType());
        copy.setValue(astNode.getValue());
        copy.setSourceLineNumber(astNode.getSourceLineNumber());
        return copy;
    }

    // Method to create a deep copy of a Beta object
    public Beta getBetaCopy(Beta beta) {
        Beta copy = new Beta();
        if (beta.getChild() != null)
            copy.setChild(beta.getChild().accept(this));
        if (beta.getSibling() != null)
            copy.setSibling(beta.getSibling().accept(this));
        copy.setType(beta.getType());
        copy.setValue(beta.getValue());
        copy.setSourceLineNumber(beta.getSourceLineNumber());

        Stack<ASTNode> thenBodyCopy = new Stack<ASTNode>();
        for (ASTNode thenBodyElement : beta.getTHEN()) {
            thenBodyCopy.add(thenBodyElement.accept(this));
        }
        copy.setTHEN(thenBodyCopy);

        Stack<ASTNode> elseBodyCopy = new Stack<ASTNode>();
        for (ASTNode elseBodyElement : beta.getELSE()) {
            elseBodyCopy.add(elseBodyElement.accept(this));
        }
        copy.setELSE(elseBodyCopy);

        return copy;
    }

    // Method to create a deep copy of an Eta object
    public Eta getEtaCopy(Eta eta) {
        Eta copy = new Eta();
        if (eta.getChild() != null)
            copy.setChild(eta.getChild().accept(this));
        if (eta.getSibling() != null)
            copy.setSibling(eta.getSibling().accept(this));
        copy.setType(eta.getType());
        copy.setValue(eta.getValue());
        copy.setSourceLineNumber(eta.getSourceLineNumber());

        copy.setDelta(eta.getDelta().accept(this));

        return copy;
    }

    // Method to create a deep copy of a Delta object
    public Delta getDeltaCopy(Delta delta) {
        Delta copy = new Delta();
        if (delta.getChild() != null)
            copy.setChild(delta.getChild().accept(this));
        if (delta.getSibling() != null)
            copy.setSibling(delta.getSibling().accept(this));
        copy.setType(delta.getType());
        copy.setValue(delta.getValue());
        copy.setIndex(delta.getIndex());
        copy.setSourceLineNumber(delta.getSourceLineNumber());

        Stack<ASTNode> bodyCopy = new Stack<ASTNode>();
        for (ASTNode bodyElement : delta.getCtrlStruct()) {
            bodyCopy.add(bodyElement.accept(this));
        }
        copy.setCtrlStruct(bodyCopy);

        List<String> boundVarsCopy = new ArrayList<String>();
        boundVarsCopy.addAll(delta.getBoundVars());
        copy.setBoundVars(boundVarsCopy);

        copy.setLinkedEnv(delta.getRunningEnv());

        return copy;
    }

    // Method to create a deep copy of a Tuple object
    public Tuple getTupleCopy(Tuple tuple) {
        Tuple copy = new Tuple();
        if (tuple.getChild() != null)
            copy.setChild(tuple.getChild().accept(this));
        if (tuple.getSibling() != null)
            copy.setSibling(tuple.getSibling().accept(this));
        copy.setType(tuple.getType());
        copy.setValue(tuple.getValue());
        copy.setSourceLineNumber(tuple.getSourceLineNumber());
        return copy;
    }
}
