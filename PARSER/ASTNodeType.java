package PARSER;

/**
 * Sort of node in an abstract syntax tree. according to the grammar for the
 * RPAL phrase structure.
 * 
 */
public enum ASTNodeType {
  // General
  IDENTIFIER("<ID:%s>"),
  STRING("<STR:'%s'>"),
  INTEGER("<INT:%s>"),

  // Expressions
  LET("let"),
  LAMBDA("lambda"),
  WHERE("where"),

  // Tuple expressions
  TAU("tau"),
  AUG("aug"),
  CONDITIONAL("->"),

  // Boolean Expressions
  OR("or"),
  AND("&"),
  NOT("not"),
  GR("gr"),
  GE("ge"),
  LS("ls"),
  LE("le"),
  EQ("eq"),
  NE("ne"),

  // Arithmetic Expressions
  PLUS("+"),
  MINUS("-"),
  NEG("neg"),
  MULT("*"),
  DIV("/"),
  EXP("**"),
  AT("@"),

  // Rators and Rands
  GAMMA("gamma"),
  TRUE("<true>"),
  FALSE("<false>"),
  NIL("<nil>"),
  DUMMY("<dummy>"),

  // Definitions
  WITHIN("within"),
  SIMULTDEF("and"),
  REC("rec"),
  EQUAL("="),
  FCNFORM("function_form"),

  // Variables
  PAREN("<()>"),
  COMMA(","),

  // Post-standardize
  YSTAR("<Y*>"),

  // For program evaluation only. Will never appear in a standardized or
  // non-standardized AST.
  BETA(""),
  DELTA(""),
  ETA(""),
  TUPLE("");

  private String printName; // used for printing AST representation

  private ASTNodeType(String name) {
    printName = name;
  }

  public String getPrintName() {
    return printName;
  }
}
