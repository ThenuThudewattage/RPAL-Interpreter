package CSE_Machine;

import java.util.Stack;

import PARSER.ASTNode;
import PARSER.ASTNodeType;

// Beta class extends ASTNode, representing a Beta node in the Abstract Syntax Tree (AST)
public class Beta extends ASTNode {

  // Stacks to hold the THEN and ELSE branches of the Beta node
  private Stack<ASTNode> ELSE;
  private Stack<ASTNode> THEN;
  
  // Constructor initializes the type of the node as BETA and creates empty stacks for THEN and ELSE branches
  public Beta() {
    setType(ASTNodeType.BETA);
    THEN = new Stack<ASTNode>();
    ELSE = new Stack<ASTNode>();
  }
  
  // Method to accept a NodeCopier visitor and return a copy of this Beta node
  public Beta accept(NodeCopier nodeCopier) {
    return nodeCopier.getBetaCopy(this);
  }
  
  // Getter method for the THEN stack
  public Stack<ASTNode> getTHEN() {
    return THEN;
  }
  
  // Getter method for the ELSE stack
  public Stack<ASTNode> getELSE() {
    return ELSE;
  }
  
  // Setter method for the THEN stack
  public void setTHEN(Stack<ASTNode> THEN) {
    this.THEN = THEN;
  }
  
  // Setter method for the ELSE stack
  public void setELSE(Stack<ASTNode> ELSE) {
    this.ELSE = ELSE;
  }

}
