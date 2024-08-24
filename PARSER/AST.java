package PARSER;

import java.util.ArrayDeque;
import java.util.Stack;

import CSE_Machine.Beta;
import CSE_Machine.Delta;

/*
  Represents an Abstract Syntax Tree (AST) with functionality to manage nodes and deltas.
  The AST class encapsulates the root node of the tree, maintains a queue of pending delta bodies,
  and tracks the standardization status. It also manages the current delta being processed,
  the root delta, and the index of the delta.
 */

public class AST {
  private ASTNode root;
  private ArrayDeque<PendingDeltaBody> pendingDeltaBodyQueue;
  private boolean standardized;
  private Delta currentDelta;
  private Delta rootOfDelta;
  private int index;

  public AST(ASTNode node) {
    this.root = node;
  }

  // there is a option which whenever inputed -ast, it will print the AST
  public void printAST() {
    preOrderPrint(root, "");
  }

  private void preOrderPrint(ASTNode node, String printPrefix) {
    if (node == null)
      return;

    printASTNodeDetails(node, printPrefix);
    preOrderPrint(node.getChild(), printPrefix + ".");
    preOrderPrint(node.getSibling(), printPrefix);
  }

  private void printASTNodeDetails(ASTNode node, String printPrefix) {
    if (node.getType() == ASTNodeType.IDENTIFIER ||
        node.getType() == ASTNodeType.INTEGER) {
      System.out.printf(printPrefix + node.getType().getPrintName() + "\n", node.getValue());
    } else if (node.getType() == ASTNodeType.STRING)
      System.out.printf(printPrefix + node.getType().getPrintName() + "\n", node.getValue());
    else
      System.out.println(printPrefix + node.getType().getPrintName());
  }

  // standardize the AST
  public void standardize() {
    standardize(root);
    standardized = true;
  }

  private void standardize(ASTNode node) { // standardize the AST node and its children
    if (node.getChild() != null) {
      ASTNode childNode = node.getChild();
      while (childNode != null) {
        standardize(childNode);
        childNode = childNode.getSibling();
      }
    }

    switch (node.getType()) {
      case LET:
        // This block transforms a LET statement into a LAMBDA-GAMMA combination.
        // It reorders the nodes to conform to the expected structure of a
        // LAMBDA-GAMMA combination,
        // where the left child becomes a LAMBDA node and the LET node itself becomes a
        // GAMMA node.

        ASTNode equalNode = node.getChild();
        if (equalNode.getType() != ASTNodeType.EQUAL)
          throw new StandardizeException(
              "LET/WHERE statement expects an EQUAL node on the left, but found different structure.");
        ASTNode e = equalNode.getChild().getSibling();
        equalNode.getChild().setSibling(equalNode.getSibling());
        equalNode.setSibling(e);
        equalNode.setType(ASTNodeType.LAMBDA);
        node.setType(ASTNodeType.GAMMA);
        break;

      case WHERE:
        // Standardize WHERE statement: Reorders nodes to convert WHERE into LET syntax
        // by transforming it into a LET statement, then recursively standardizes the
        // resulting LET node.
        equalNode = node.getChild().getSibling();
        node.getChild().setSibling(null);
        equalNode.setSibling(node.getChild());
        node.setChild(equalNode);
        node.setType(ASTNodeType.LET);
        standardize(node);
        break;

      case FCNFORM:
        // Syntax transformation: FCNFORM EQUAL -> Parameter Value+ Expression ->
        // Parameter + LAMBDA Expression
        ASTNode childSibling = node.getChild().getSibling();
        node.getChild().setSibling(constructLambdaChain(childSibling));
        node.setType(ASTNodeType.EQUAL);
        break;

      case AT:
        // Syntax transformation: AT GAMMA -> E1 N E2 GAMMA E2 -> N E1
        ASTNode e1 = node.getChild();
        ASTNode n = e1.getSibling();
        ASTNode e2 = n.getSibling();
        ASTNode gammaNode = new ASTNode();
        gammaNode.setType(ASTNodeType.GAMMA);
        gammaNode.setChild(n);
        n.setSibling(e1);
        e1.setSibling(null);
        gammaNode.setSibling(e2);
        node.setChild(gammaNode);
        node.setType(ASTNodeType.GAMMA);
        break;

      case WITHIN:
        // Syntax transformation: WITHIN EQUAL -> X2 GAMMA -> X1 E1 X2 E2 LAMBDA E1 ->
        // X1 E2
        if (node.getChild().getType() != ASTNodeType.EQUAL
            || node.getChild().getSibling().getType() != ASTNodeType.EQUAL)
          throw new StandardizeException(
              "WITHIN: One of the child nodes does not conform to the expected EQUAL structure.");
        ASTNode x1 = node.getChild().getChild();
        e1 = x1.getSibling();
        ASTNode x2 = node.getChild().getSibling().getChild();
        e2 = x2.getSibling();
        ASTNode lambdaNode = new ASTNode();
        lambdaNode.setType(ASTNodeType.LAMBDA);
        x1.setSibling(e2);
        lambdaNode.setChild(x1);
        lambdaNode.setSibling(e1);
        gammaNode = new ASTNode();
        gammaNode.setType(ASTNodeType.GAMMA);
        gammaNode.setChild(lambdaNode);
        x2.setSibling(gammaNode);
        node.setChild(x2);
        node.setType(ASTNodeType.EQUAL);
        break;

      case SIMULTDEF:
        // Syntax transformation: SIMULTDEF EQUAL -> COMMA TAU -> V1 E1, ..., Vn En ; ->
        // V1 E1, ..., Vn En
        ASTNode commaNode = new ASTNode();
        commaNode.setType(ASTNodeType.COMMA);
        ASTNode tauNode = new ASTNode();
        tauNode.setType(ASTNodeType.TAU);
        ASTNode childNode = node.getChild();
        while (childNode != null) {
          commaTauNodes(childNode, commaNode, tauNode);
          childNode = childNode.getSibling();
        }
        commaNode.setSibling(tauNode);
        node.setChild(commaNode);
        node.setType(ASTNodeType.EQUAL);
        break;

      case REC:
        // Standardize REC statement: Transforms a recursive definition into an
        // equivalent form for execution using Y combinator.
        // Syntax transformation: REC EQUAL -> X GAMMA -> X Y* LAMBDA -> X* GAMMA -> V1
        // E1, ..., Vn En
        childNode = node.getChild();
        if (childNode.getType() != ASTNodeType.EQUAL)
          throw new StandardizeException("REC: The child node does not adhere to the expected EQUAL structure.");
        ASTNode x = childNode.getChild();
        lambdaNode = new ASTNode();
        lambdaNode.setType(ASTNodeType.LAMBDA);
        lambdaNode.setChild(x);
        ASTNode yStarNode = new ASTNode();
        yStarNode.setType(ASTNodeType.YSTAR);
        yStarNode.setSibling(lambdaNode);
        gammaNode = new ASTNode();
        gammaNode.setType(ASTNodeType.GAMMA);
        gammaNode.setChild(yStarNode);
        ASTNode xWithSiblingGamma = new ASTNode();
        xWithSiblingGamma.setChild(x.getChild());
        xWithSiblingGamma.setSibling(gammaNode);
        xWithSiblingGamma.setType(x.getType());
        xWithSiblingGamma.setValue(x.getValue());
        node.setChild(xWithSiblingGamma);
        node.setType(ASTNodeType.EQUAL);
        break;

      case LAMBDA:
        // Standardize LAMBDA statement: Constructs a lambda chain for multiple
        // parameters.
        // Syntax transformation: LAMBDA -> V1 V2 ... Vn
        childSibling = node.getChild().getSibling();
        node.getChild().setSibling(constructLambdaChain(childSibling));
        break;

      default:
        break;
    }
  }

  private void commaTauNodes(ASTNode equalNode, ASTNode commaNode, ASTNode tauNode) {
    if (equalNode.getType() != ASTNodeType.EQUAL)
      throw new StandardizeException(
          "SIMULTDEF: One of the child nodes does not adhere to the expected EQUAL structure.");
    ASTNode x = equalNode.getChild();
    ASTNode e = x.getSibling();
    addChild(commaNode, x);
    addChild(tauNode, e);
  }

  // Add a child node to the parent node
  private void addChild(ASTNode parentNode, ASTNode childNode) {
    if (parentNode.getChild() == null)
      parentNode.setChild(childNode);
    else {
      ASTNode lastSibling = parentNode.getChild();
      while (lastSibling.getSibling() != null)
        lastSibling = lastSibling.getSibling();
      lastSibling.setSibling(childNode);
    }
    childNode.setSibling(null);
  }

  // Construct a lambda chain for multiple parameters
  private ASTNode constructLambdaChain(ASTNode node) {
    if (node.getSibling() == null)
      return node;

    ASTNode lambdaNode = new ASTNode();
    lambdaNode.setType(ASTNodeType.LAMBDA);
    lambdaNode.setChild(node);
    if (node.getSibling().getSibling() != null)
      node.setSibling(constructLambdaChain(node.getSibling()));
    return lambdaNode;
  }

  public Delta createDeltas() {
    pendingDeltaBodyQueue = new ArrayDeque<PendingDeltaBody>();
    index = 0;
    currentDelta = createDelta(root);
    processPendingDeltaStack();
    return rootOfDelta;
  }

  private Delta createDelta(ASTNode startBodyNode) {
    PendingDeltaBody pendingDelta = new PendingDeltaBody();
    pendingDelta.startNode = startBodyNode;
    pendingDelta.body = new Stack<ASTNode>();
    pendingDeltaBodyQueue.add(pendingDelta);

    Delta delta = new Delta();
    delta.setCtrlStruct(pendingDelta.body);
    delta.setIndex(index++);
    currentDelta = delta;

    if (startBodyNode == root)
      rootOfDelta = currentDelta;

    return delta;
  }

  // Process the pending delta stack
  private void processPendingDeltaStack() {
    while (!pendingDeltaBodyQueue.isEmpty()) {
      PendingDeltaBody pendingDeltaBody = pendingDeltaBodyQueue.pop();
      buildDeltaBody(pendingDeltaBody.startNode, pendingDeltaBody.body);
    }
  }

  private void buildDeltaBody(ASTNode node, Stack<ASTNode> body) {
    /**
     * Recursively builds the delta body stack for the given AST node, handling
     * special cases for lambda and conditional nodes.
     * 
     * @param node The current AST node being processed.
     * @param body The stack to which the delta body nodes are pushed.
     */
    if (node.getType() == ASTNodeType.LAMBDA) {
      Delta d = createDelta(node.getChild().getSibling());
      if (node.getChild().getType() == ASTNodeType.COMMA) {
        ASTNode commaNode = node.getChild();
        ASTNode childNode = commaNode.getChild();
        while (childNode != null) {
          d.appendBddVars(childNode.getValue());
          childNode = childNode.getSibling();
        }
      } else
        d.appendBddVars(node.getChild().getValue());
      body.push(d);
      return;
    } else if (node.getType() == ASTNodeType.CONDITIONAL) {
      ASTNode conditionNode = node.getChild();
      ASTNode thenNode = conditionNode.getSibling();
      ASTNode elseNode = thenNode.getSibling();
      Beta betaNode = new Beta();

      buildDeltaBody(thenNode, betaNode.getTHEN());
      buildDeltaBody(elseNode, betaNode.getELSE());

      body.push(betaNode);

      buildDeltaBody(conditionNode, body);

      return;
    }

    body.push(node);
    ASTNode childNode = node.getChild();
    while (childNode != null) {
      buildDeltaBody(childNode, body);
      childNode = childNode.getSibling();
    }
  }

  private class PendingDeltaBody {
    Stack<ASTNode> body;
    ASTNode startNode;
  }

  public boolean isStandardized() {
    return standardized;
  }
}
