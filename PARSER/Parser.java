package PARSER;

import java.util.Stack;

import SCANNER.Scanner;
import SCANNER.Token;
import SCANNER.TokenType;

// Represents a parser that builds an Abstract Syntax Tree (AST) from a sequence of tokens.
public class Parser {
  private Scanner s;
  private Token currentToken;
  Stack<ASTNode> stack;

  public Parser(Scanner s) {
    // Initialize the parser with the given scanner.
    this.s = s;
    stack = new Stack<ASTNode>();
  }

  public AST buildAST() {
    beginParse(); // Start the parsing process.
    return new AST(stack.pop());
  }

  public void beginParse() {
    // Start the parsing process by reading the first token.
    readPop();
    E();
    if (currentToken != null)
      throw new ParseException("Expected EOF.");
  }

  private void readPop() {
    do {
      // get the next token and ignore comments and delete tokens
      currentToken = s.readNextToken();
    } while (isCurrentTokenType(TokenType.DELETE));

    if (null != currentToken) {
      if (currentToken.getType() == TokenType.IDENTIFIER) {
        createTerminalASTNode(ASTNodeType.IDENTIFIER, currentToken.getValue());
      } else if (currentToken.getType() == TokenType.INTEGER) {
        createTerminalASTNode(ASTNodeType.INTEGER, currentToken.getValue());
      } else if (currentToken.getType() == TokenType.STRING) {
        createTerminalASTNode(ASTNodeType.STRING, currentToken.getValue());
      }
    }
  }

  private boolean isCurrentToken(TokenType type, String value) {
    // Check if the current token matches the given type and value.
    if (currentToken == null)
      return false;
    if (currentToken.getType() != type || !currentToken.getValue().equals(value))
      return false;
    return true;
  }

  private boolean isCurrentTokenType(TokenType type) {
    // Check if the current token matches the given type.
    if (currentToken == null)
      return false;
    if (currentToken.getType() == type)
      return true;
    return false;
  }

  private void buildNAryASTNode(ASTNodeType type, int numOfChildren) {
    // Pop the top n nodes from the stack and create a new AST node with the given
    // type.
    ASTNode node = new ASTNode();
    node.setType(type);
    while (numOfChildren > 0) {
      ASTNode child = stack.pop();
      if (node.getChild() != null) // add the child to the front of the list
        child.setSibling(node.getChild());
      node.setChild(child);
      node.setSourceLineNumber(child.getSourceLineNumber());
      numOfChildren--;
    }
    stack.push(node);
  }

  private void createTerminalASTNode(ASTNodeType type, String value) {
    // Create a new terminal AST node with the given type and value.
    ASTNode node = new ASTNode();
    node.setType(type);
    node.setValue(value);
    node.setSourceLineNumber(currentToken.getSourceLineNumber());
    stack.push(node);
  }

  /*
   * ---------Expressions ---------
   *
   * E -> ’let’ D ’in’ E => ’let’
   * -> ’fn’ Vb+ ’.’ E => ’lambda’
   * -> Ew;
   * 
   * Parses the E non-terminal, which represents an expression in the language
   * grammar.
   * Handles cases for let expressions and lambda functions, recursively parsing
   * sub-expressions.
   */

  private void E() {
    if (isCurrentToken(TokenType.KEYWORD, "let")) {
      readPop();
      D();
      if (!isCurrentToken(TokenType.KEYWORD, "in"))
        throw new ParseException("E:  'in' expected"); // check for 'in'
      readPop();
      E();
      buildNAryASTNode(ASTNodeType.LET, 2);
    } else if (isCurrentToken(TokenType.KEYWORD, "fn")) {
      int treesToPop = 0;

      readPop();
      while (isCurrentTokenType(TokenType.IDENTIFIER) || isCurrentTokenType(TokenType.L_PAREN)) {
        VB();
        treesToPop++;
      }

      if (treesToPop == 0)
        throw new ParseException("E: at least one 'Vb' expected");

      if (!isCurrentToken(TokenType.OPERATOR, "."))
        throw new ParseException("E: '.' expected");

      readPop();
      E();

      buildNAryASTNode(ASTNodeType.LAMBDA, treesToPop + 1);
    } else
      EW();
  }

  /*
   * Ew -> T ’where’ Dr => ’where’
   * -> T;
   * Parses the Ew non-terminal, which represents an expression with a where
   * clause in the language grammar.
   * Handles cases for expressions with where clauses, recursively parsing
   * sub-expressions.
   */

  private void EW() {
    T();
    if (isCurrentToken(TokenType.KEYWORD, "where")) {
      readPop();
      DR();
      buildNAryASTNode(ASTNodeType.WHERE, 2);
    }
  }

  /*
   * ----------Tuple Expresssions-------
   *
   * T -> Ta ( ’,’ Ta )+ => ’tau’
   * -> Ta ;
   * 
   * Parses the T non-terminal, which represents a tuple in the language grammar.
   */
  private void T() {
    TA();
    int treesToPop = 0;
    while (isCurrentToken(TokenType.OPERATOR, ",")) {
      readPop();
      TA();
      treesToPop++;
    }
    if (treesToPop > 0)
      buildNAryASTNode(ASTNodeType.TAU, treesToPop + 1);
  }

  /*
   * Ta -> Ta ’aug’ Tc => ’aug’
   * -> Tc ;
   */

  private void TA() {
    TC();

    while (isCurrentToken(TokenType.KEYWORD, "aug")) {
      readPop();
      TC();
      buildNAryASTNode(ASTNodeType.AUG, 2);
    }
  }

  /*
   * Tc -> B ’->’ Tc ’|’ Tc => ’->’
   * -> B ;
   */

  private void TC() {
    B();
    if (isCurrentToken(TokenType.OPERATOR, "->")) {
      readPop();
      TC();
      if (!isCurrentToken(TokenType.OPERATOR, "|"))
        throw new ParseException("TC: '|' expected");
      readPop();
      TC();
      buildNAryASTNode(ASTNodeType.CONDITIONAL, 3);
    }
  }

  /*
   * --------Boolean Expressions ----------
   * B ->B’or’ Bt => ’or’
   * -> Bt ;
   */

  private void B() {
    BT();
    while (isCurrentToken(TokenType.KEYWORD, "or")) {
      readPop();
      BT();
      buildNAryASTNode(ASTNodeType.OR, 2);
    }
  }

  /*
   * Bt -> Bt ’&’ Bs => ’&’
   * -> Bs ;
   */

  private void BT() {
    BS();
    while (isCurrentToken(TokenType.OPERATOR, "&")) {
      readPop();
      BS();
      buildNAryASTNode(ASTNodeType.AND, 2);
    }
  }

  /*
   * Bs -> ’not’ Bp => ’not’
   * -> Bp ;
   */
  private void BS() {
    if (isCurrentToken(TokenType.KEYWORD, "not")) {
      readPop();
      BP();
      buildNAryASTNode(ASTNodeType.NOT, 1);
    } else
      BP();
  }

  /*
   * Bp -> A (’gr’ | ’>’ ) A => ’gr’
   * -> A (’ge’ | ’>=’) A => ’ge’
   * -> A (’ls’ | ’<’ ) A => ’ls’
   * -> A (’le’ | ’<=’) A => ’le’
   * -> A ’eq’ A => ’eq’
   * -> A ’ne’ A => ’ne’
   * -> A ;
   */

  private void BP() {
    A();
    if (isCurrentToken(TokenType.KEYWORD, "gr") || isCurrentToken(TokenType.OPERATOR, ">")) {

      readPop();
      A();
      buildNAryASTNode(ASTNodeType.GR, 2);
    } else if (isCurrentToken(TokenType.KEYWORD, "ge") || isCurrentToken(TokenType.OPERATOR, ">=")) {

      readPop();
      A();
      buildNAryASTNode(ASTNodeType.GE, 2);
    } else if (isCurrentToken(TokenType.KEYWORD, "ls") || isCurrentToken(TokenType.OPERATOR, "<")) {

      readPop();
      A();
      buildNAryASTNode(ASTNodeType.LS, 2);
    } else if (isCurrentToken(TokenType.KEYWORD, "le") || isCurrentToken(TokenType.OPERATOR, "<=")) {

      readPop();
      A();
      buildNAryASTNode(ASTNodeType.LE, 2);
    } else if (isCurrentToken(TokenType.KEYWORD, "eq")) {
      readPop();
      A();
      buildNAryASTNode(ASTNodeType.EQ, 2);
    } else if (isCurrentToken(TokenType.KEYWORD, "ne")) {
      readPop();
      A();
      buildNAryASTNode(ASTNodeType.NE, 2);
    }
  }

  /*
   * --------Arithmmetic Expressions------------
   * A ->A’+’ At => ’+’
   * -> A ’-’ At => ’-’
   * -> ’+’ At
   * -> ’-’ At => ’neg’
   * -> At ;
   */
  private void A() {
    if (isCurrentToken(TokenType.OPERATOR, "+")) {
      readPop();
      AT();
    } else if (isCurrentToken(TokenType.OPERATOR, "-")) {
      readPop();
      AT();
      buildNAryASTNode(ASTNodeType.NEG, 1);
    } else
      AT();

    boolean plus = true;
    while (isCurrentToken(TokenType.OPERATOR, "+") || isCurrentToken(TokenType.OPERATOR, "-")) {
      if (currentToken.getValue().equals("+"))
        plus = true;
      else if (currentToken.getValue().equals("-"))
        plus = false;
      readPop();
      AT();
      if (plus)
        buildNAryASTNode(ASTNodeType.PLUS, 2);
      else
        buildNAryASTNode(ASTNodeType.MINUS, 2);
    }
  }

  /*
   * At -> At ’*’ Af => ’*’
   * -> At ’/’ Af => ’/’
   * -> Af ;
   */

  private void AT() {
    AF();
    boolean mult = true;
    while (isCurrentToken(TokenType.OPERATOR, "*") || isCurrentToken(TokenType.OPERATOR, "/")) {
      if (currentToken.getValue().equals("*"))
        mult = true;
      else if (currentToken.getValue().equals("/"))
        mult = false;
      readPop();
      AF();
      if (mult)
        buildNAryASTNode(ASTNodeType.MULT, 2);
      else
        buildNAryASTNode(ASTNodeType.DIV, 2);
    }
  }

  /*
   * Af -> Ap ’**’ Af => ’**’
   * -> Ap ;
   */
  private void AF() {
    AP();
    if (isCurrentToken(TokenType.OPERATOR, "**")) {
      readPop();
      AF();
      buildNAryASTNode(ASTNodeType.EXP, 2);
    }
  }

  /*
   * Ap -> Ap ’@’ ’<IDENTIFIER>’ R => ’@’
   * -> R ;
   */
  private void AP() {
    R();
    while (isCurrentToken(TokenType.OPERATOR, "@")) {
      readPop();
      if (!isCurrentTokenType(TokenType.IDENTIFIER))
        throw new ParseException("AP: expected Identifier");
      readPop();
      R();
      buildNAryASTNode(ASTNodeType.AT, 3);
    }
  }

  /*
   * ---------Operators and Operands----------
   * R ->RRn => ’gamma’
   * -> Rn ;
   */
  private void R() {
    RN();
    readPop();
    while (isCurrentTokenType(TokenType.INTEGER) ||
        isCurrentTokenType(TokenType.STRING) ||
        isCurrentTokenType(TokenType.IDENTIFIER) ||
        isCurrentToken(TokenType.KEYWORD, "true") ||
        isCurrentToken(TokenType.KEYWORD, "false") ||
        isCurrentToken(TokenType.KEYWORD, "nil") ||
        isCurrentToken(TokenType.KEYWORD, "dummy") ||
        isCurrentTokenType(TokenType.L_PAREN)) {
      RN();
      buildNAryASTNode(ASTNodeType.GAMMA, 2);
      readPop();
    }
  }

  /*
   * Rn -> ’<IDENTIFIER>’
   * -> ’<INTEGER>’
   * -> ’<STRING>’
   * -> ’true’ => ’true’
   * -> ’false’ => ’false’
   * -> ’nil’ => ’nil’
   * -> ’(’ E ’)’
   * -> ’dummy’ => ’dummy’ ;
   * 
   */
  private void RN() {
    if (isCurrentTokenType(TokenType.IDENTIFIER) ||
        isCurrentTokenType(TokenType.INTEGER) ||
        isCurrentTokenType(TokenType.STRING)) {
    } else if (isCurrentToken(TokenType.KEYWORD, "true")) {
      createTerminalASTNode(ASTNodeType.TRUE, "true");
    } else if (isCurrentToken(TokenType.KEYWORD, "false")) {
      createTerminalASTNode(ASTNodeType.FALSE, "false");
    } else if (isCurrentToken(TokenType.KEYWORD, "nil")) {
      createTerminalASTNode(ASTNodeType.NIL, "nil");
    } else if (isCurrentTokenType(TokenType.L_PAREN)) {
      readPop();
      E();
      if (!isCurrentTokenType(TokenType.R_PAREN))
        throw new ParseException("RN: ')' expected");
    } else if (isCurrentToken(TokenType.KEYWORD, "dummy")) {
      createTerminalASTNode(ASTNodeType.DUMMY, "dummy");
    }
  }

  /*
   * --------Definitions--------
   * D -> Da ’within’ D => ’within’
   * -> Da ;
   * 
   */
  private void D() {
    DA();
    if (isCurrentToken(TokenType.KEYWORD, "within")) {
      readPop();
      D();
      buildNAryASTNode(ASTNodeType.WITHIN, 2);
    }
  }

  /*
   * Da -> Dr ( ’and’ Dr )+ => ’and’
   * -> Dr ;
   */
  private void DA() {
    DR();
    int treesToPop = 0;
    while (isCurrentToken(TokenType.KEYWORD, "and")) {
      readPop();
      DR();
      treesToPop++;
    }
    if (treesToPop > 0)
      buildNAryASTNode(ASTNodeType.SIMULTDEF, treesToPop + 1);
  }

  /*
   * Dr -> ’rec’ Db => ’rec’
   */
  private void DR() {
    if (isCurrentToken(TokenType.KEYWORD, "rec")) {
      readPop();
      DB();
      buildNAryASTNode(ASTNodeType.REC, 1);
    } else {
      DB();
    }
  }

  /*
   * Db -> Vl ’=’ E => ’=’
   * -> ’<IDENTIFIER>’ Vb+ ’=’ E => ’fcn_form’
   * -> ’(’ D ’)’ ;
   */
  private void DB() {
    if (isCurrentTokenType(TokenType.L_PAREN)) {
      D();
      readPop();
      if (!isCurrentTokenType(TokenType.R_PAREN))
        throw new ParseException("DB: ')' expected");
      readPop();
    } else if (isCurrentTokenType(TokenType.IDENTIFIER)) {
      readPop();
      if (isCurrentToken(TokenType.OPERATOR, ",")) {
        readPop();
        VL();
        if (!isCurrentToken(TokenType.OPERATOR, "="))
          throw new ParseException("DB: = expected.");
        buildNAryASTNode(ASTNodeType.COMMA, 2);
        readPop();
        E();
        buildNAryASTNode(ASTNodeType.EQUAL, 2);
      } else {
        if (isCurrentToken(TokenType.OPERATOR, "=")) {
          readPop();
          E();
          buildNAryASTNode(ASTNodeType.EQUAL, 2);
        } else {
          int treesToPop = 0;

          while (isCurrentTokenType(TokenType.IDENTIFIER) || isCurrentTokenType(TokenType.L_PAREN)) {
            VB();
            treesToPop++;
          }

          if (treesToPop == 0)
            throw new ParseException("E: at least one 'Vb' expected");

          if (!isCurrentToken(TokenType.OPERATOR, "="))
            throw new ParseException("DB: = expected.");

          readPop();
          E();

          buildNAryASTNode(ASTNodeType.FCNFORM, treesToPop + 2);
        }
      }
    }
  }

  /*
   * ----------- Variables ----------
   * Vb -> ’<IDENTIFIER>’
   * -> ’(’ Vl ’)’
   * -> ’(’ ’)’ => ’()’;
   */
  private void VB() {
    if (isCurrentTokenType(TokenType.IDENTIFIER)) {
      readPop();
    } else if (isCurrentTokenType(TokenType.L_PAREN)) {
      readPop();
      if (isCurrentTokenType(TokenType.R_PAREN)) {
        createTerminalASTNode(ASTNodeType.PAREN, "");
        readPop();
      } else {
        VL();
        if (!isCurrentTokenType(TokenType.R_PAREN))
          throw new ParseException("VB: ')' expected");
        readPop();
      }
    }
  }

  /*
   * Vl -> ’<IDENTIFIER>’ list ’,’ => ’,’?;
   */
  private void VL() {
    if (!isCurrentTokenType(TokenType.IDENTIFIER))
      throw new ParseException("VL: Identifier expected");
    else {
      readPop();
      int treesToPop = 0;
      while (isCurrentToken(TokenType.OPERATOR, ",")) {
        readPop();
        if (!isCurrentTokenType(TokenType.IDENTIFIER))
          throw new ParseException("VL: Identifier expected");
        readPop();
        treesToPop++;
      }
      if (treesToPop > 0)
        buildNAryASTNode(ASTNodeType.COMMA, treesToPop + 1);
    }
  }

}
