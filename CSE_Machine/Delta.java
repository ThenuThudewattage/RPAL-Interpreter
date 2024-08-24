package CSE_Machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import PARSER.ASTNode;
import PARSER.ASTNodeType;

// Delta class representing a delta node in the abstract syntax tree
public class Delta extends ASTNode {
  private List<String> boundVars;  // List of bound variables
  private Environment linkedEnv; 
  private Stack<ASTNode> body;
  private int index;

  public Delta() {
    setType(ASTNodeType.DELTA);
    boundVars = new ArrayList<String>();
  }
  
  // Method to accept a NodeCopier visitor

  public Delta accept(NodeCopier nodeCopier) {
    return nodeCopier.getDeltaCopy(this);
  }
  
  // Override method to get the value of the delta node
  @Override
  public String getValue() {
    return "[lambda closure: " + boundVars.get(0) + ": " + index + "]";
  }

  public List<String> getBoundVars() {
    return boundVars;
  }
  
  // Method to append a bound variable to the list
  public void appendBddVars(String boundVar) {
    boundVars.add(boundVar);
  }
  
  // Setter method for the list of bound variables
  public void setBoundVars(List<String> boundVars) {
    this.boundVars = boundVars;
  }
  
  // Getter method for the body of the delta node
  public Stack<ASTNode> getCtrlStruct() {
    return body;
  }
  
  // Setter method for the body of the delta node
  public void setCtrlStruct(Stack<ASTNode> body) {
    this.body = body;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public Environment getRunningEnv() {
    return linkedEnv;
  }
  
  // Setter method for the running environment linked to the delta node
  public void setLinkedEnv(Environment linkedEnv) {
    this.linkedEnv = linkedEnv;
  }
}
