package PARSER;

import CSE_Machine.NodeCopier;

// Represents a node in the Abstract Syntax Tree (AST)
public class ASTNode {
  private ASTNodeType type; // Type of the AST node
  private String value; // Value associated with the node
  private ASTNode child; // Child node
  private ASTNode sibling; // Sibling node
  private int sourceLineNumber; // Source line number where the node appears

  // Get the name of the AST node type
  public String getName() {
    return type.name();
  }

  // Get the type of the AST node
  public ASTNodeType getType() {
    return type;
  }

  // Set the type of the AST node
  public void setType(ASTNodeType type) {
    this.type = type;
  }

  // Get the child node of the AST node
  public ASTNode getChild() {
    return child;
  }

  // Set the child node of the AST node
  public void setChild(ASTNode child) {
    this.child = child;
  }

  // Get the sibling node of the AST node
  public ASTNode getSibling() {
    return sibling;
  }

  // Set the sibling node of the AST node
  public void setSibling(ASTNode sibling) {
    this.sibling = sibling;
  }

  // Get the value associated with the AST node
  public String getValue() {
    return value;
  }

  // Set the value associated with the AST node
  public void setValue(String value) {
    this.value = value;
  }

  // Accept a NodeCopier visitor to perform a copy operation on the AST node
  public ASTNode accept(NodeCopier nodeCopier) {
    return nodeCopier.copy(this);
  }

  // Get the source line number where the AST node appears
  public int getSourceLineNumber() {
    return sourceLineNumber;
  }

  // Set the source line number where the AST node appears
  public void setSourceLineNumber(int sourceLineNumber) {
    this.sourceLineNumber = sourceLineNumber;
  }
}