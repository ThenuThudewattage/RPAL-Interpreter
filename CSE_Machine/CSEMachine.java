package CSE_Machine;

import java.util.Stack;

import PARSER.AST;
import PARSER.ASTNode;
import PARSER.ASTNodeType;

// CSEMachine class represents the environment and evaluation mechanism of the CSE machine
public class CSEMachine {

  private Stack<ASTNode> valueStack;  //Stack to hold the values during evaluation
  private Delta rootDelta;   //Root delta node representing the main program
  
  // Constructor initializes the CSE machine with the given AST
  public CSEMachine(AST ast) {
    if (!ast.isStandardized())  // Check if the AST has been standardized
      throw new RuntimeException("ERROR: AST has not been Standardized!");
    
    rootDelta = ast.createDeltas();       // Create root delta node from the AST and set the primitive environment

    rootDelta.setLinkedEnv(new Environment()); 
    valueStack = new Stack<ASTNode>();  // Initialize value stack
  }

  public void evaluateProgram() {  // Method to start evaluating the program
    processControlStack(rootDelta, rootDelta.getRunningEnv());
  }

  private void processControlStack(Delta delta, Environment environment) {   // Method to process the control stack

    // Create a local stack and add control structures from the delta
    Stack<ASTNode> stack = new Stack<ASTNode>();
    stack.addAll(delta.getCtrlStruct());
    
    // Evaluate each node in the stack
    while (!stack.isEmpty())
      evaluateTopNode(delta, environment, stack);
  }
  
  // Method to evaluate the top node in the control stack
  private void evaluateTopNode(Delta delta, Environment environment, Stack<ASTNode> ctrlStack) {
    ASTNode node = ctrlStack.pop();  // Pop the top node from the stack
    if (perform_BinaryOperations(node))   // Check if the node represents binary operations
      return;
    else if (apply_UnaryOperation(node))   // Check if the node represents unary operations
      return;
    else {
      // Evaluate based on the node type
      switch (node.getType()) {
        case IDENTIFIER:
          handle_Identifiers(node, environment);
          break;
        case NIL:
        case TAU:
          evaluateTAU_Node(node);
          break;
        case BETA:
          evaluate_BetaNode((Beta) node, ctrlStack);
          break;
        case GAMMA:
          applyRandToRator(node, delta, environment, ctrlStack);
          break;
        case DELTA:
          ((Delta) node).setLinkedEnv(environment); // RULE 2
          valueStack.push(node);
          break;
        default:
          // Push the node to the value stack
          valueStack.push(node);
          break;
      }
    }
  }

  // Method to perform binary operations
  private boolean perform_BinaryOperations(ASTNode rator) {
    switch (rator.getType()) {   // Handle different binary operations
      case PLUS:
      case MINUS:
      case MULT:
      case DIV:
      case EXP:
      case LS:
      case LE:
      case GR:
      case GE:
        bin_ari_operations(rator.getType());
        return true;
      case EQ:
      case NE:
        EQ_NE_operators(rator.getType());
        return true;
      case OR:
      case AND:
        AND_OR_operations(rator.getType());
        return true;
      case AUG:
        extend_Tuples();
        return true;
      default:
        return false;
    }
  }
  
  // Method to perform binary arithmetic operations
  private void bin_ari_operations(ASTNodeType type) {
    // Pop operands from the value stack
    ASTNode rand_1 = valueStack.pop();  
    ASTNode rand_2 = valueStack.pop();
    if (rand_1.getType() != ASTNodeType.INTEGER || rand_2.getType() != ASTNodeType.INTEGER)
      SyntaxError.printError(rand_1.getSourceLineNumber(),
          "Expected two integers; was given \"" + rand_1.getValue() + "\", \"" + rand_2.getValue() + "\"");

    ASTNode result = new ASTNode();
    result.setType(ASTNodeType.INTEGER);

    switch (type) {
      case PLUS:
        result.setValue(Integer.toString(Integer.parseInt(rand_1.getValue()) + Integer.parseInt(rand_2.getValue())));
        break;
      case MINUS:
        result.setValue(Integer.toString(Integer.parseInt(rand_1.getValue()) - Integer.parseInt(rand_2.getValue())));
        break;
      case MULT:
        result.setValue(Integer.toString(Integer.parseInt(rand_1.getValue()) * Integer.parseInt(rand_2.getValue())));
        break;
      case DIV:
        result.setValue(Integer.toString(Integer.parseInt(rand_1.getValue()) / Integer.parseInt(rand_2.getValue())));
        break;
      case EXP:
        result.setValue(
            Integer.toString((int) Math.pow(Integer.parseInt(rand_1.getValue()), Integer.parseInt(rand_2.getValue()))));
        break;
      case LS:
        if (Integer.parseInt(rand_1.getValue()) < Integer.parseInt(rand_2.getValue()))
          True_Push();
        else
          False_Push();
        return;
      case LE:
        if (Integer.parseInt(rand_1.getValue()) <= Integer.parseInt(rand_2.getValue()))
          True_Push();
        else
          False_Push();
        return;
      case GR:
        if (Integer.parseInt(rand_1.getValue()) > Integer.parseInt(rand_2.getValue()))
          True_Push();
        else
          False_Push();
        return;
      case GE:
        if (Integer.parseInt(rand_1.getValue()) >= Integer.parseInt(rand_2.getValue()))
          True_Push();
        else
          False_Push();
        return;
      default:
        break;
    }
    valueStack.push(result);
  }
  
  // Method to perform equality and inequality operations
  private void EQ_NE_operators(ASTNodeType type) {
    ASTNode rand_1 = valueStack.pop();
    ASTNode rand_2 = valueStack.pop();

    if (rand_1.getType() == ASTNodeType.TRUE || rand_1.getType() == ASTNodeType.FALSE) {
      if (rand_2.getType() != ASTNodeType.TRUE && rand_2.getType() != ASTNodeType.FALSE)
        SyntaxError.printError(rand_1.getSourceLineNumber(),
            "Cannot compare dissimilar types; was given \"" + rand_1.getValue() + "\", \"" + rand_2.getValue() + "\"");
      cmpBool_Values(rand_1, rand_2, type);
      return;
    }

    if (rand_1.getType() != rand_2.getType())
      SyntaxError.printError(rand_1.getSourceLineNumber(),
          "Cannot compare dissimilar types; was given \"" + rand_1.getValue() + "\", \"" + rand_2.getValue() + "\"");

    if (rand_1.getType() == ASTNodeType.STRING)
      cmp_Str(rand_1, rand_2, type);
    else if (rand_1.getType() == ASTNodeType.INTEGER)
      cmp_Int(rand_1, rand_2, type);
    else
      SyntaxError.printError(rand_1.getSourceLineNumber(),
          "Don't know how to " + type + " \"" + rand_1.getValue() + "\", \"" + rand_2.getValue() + "\"");

  }

  private void cmpBool_Values(ASTNode rand1, ASTNode rand2, ASTNodeType type) {
    if (rand1.getType() == rand2.getType())
      if (type == ASTNodeType.EQ)
        True_Push();
      else
        False_Push();
    else if (type == ASTNodeType.EQ)
      False_Push();
    else
      True_Push();
  }
  // Method to compare string operations
  private void cmp_Str(ASTNode rand1, ASTNode rand2, ASTNodeType type) {
    if (rand1.getValue().equals(rand2.getValue()))
      if (type == ASTNodeType.EQ)
        True_Push();
      else
        False_Push();
    else if (type == ASTNodeType.EQ)
      False_Push();
    else
      True_Push();
  }
  
  // Method to compare integer operations
  private void cmp_Int(ASTNode rand1, ASTNode rand2, ASTNodeType type) {
    if (Integer.parseInt(rand1.getValue()) == Integer.parseInt(rand2.getValue()))
      if (type == ASTNodeType.EQ)
        True_Push();
      else
        False_Push();
    else if (type == ASTNodeType.EQ)
      False_Push();
    else
      True_Push();
  }
  
  // Method to perform logical AND and OR operations
  private void AND_OR_operations(ASTNodeType type) {
    ASTNode rand_1 = valueStack.pop();
    ASTNode rand_2 = valueStack.pop();

    if ((rand_1.getType() == ASTNodeType.TRUE || rand_1.getType() == ASTNodeType.FALSE) &&
        (rand_2.getType() == ASTNodeType.TRUE || rand_2.getType() == ASTNodeType.FALSE)) {
      logical_Operators(rand_1, rand_2, type);
      return;
    }

    SyntaxError.printError(rand_1.getSourceLineNumber(),
        "Cannot " + type + " \"" + rand_1.getValue() + "\", \"" + rand_2.getValue() + "\"");
  }
  
  // Method to perform logical operations
  private void logical_Operators(ASTNode rand1, ASTNode rand2, ASTNodeType type) {
    if (type == ASTNodeType.OR) {
      if (rand1.getType() == ASTNodeType.TRUE || rand2.getType() == ASTNodeType.TRUE)
        True_Push();
      else
        False_Push();
    } else {
      if (rand1.getType() == ASTNodeType.TRUE && rand2.getType() == ASTNodeType.TRUE)
        True_Push();
      else
        False_Push();
    }
  }
  
  // Method to extend tuples
  private void extend_Tuples() {
    ASTNode rand1 = valueStack.pop();
    ASTNode rand2 = valueStack.pop();

    if (rand1.getType() != ASTNodeType.TUPLE)
      SyntaxError.printError(rand1.getSourceLineNumber(),
          "Cannot append to a non-tuple \"" + rand1.getValue() + "\"");

    ASTNode childNode = rand1.getChild();
    if (childNode == null)
      rand1.setChild(rand2);
    else {
      while (childNode.getSibling() != null)
        childNode = childNode.getSibling();
      childNode.setSibling(rand2);
    }
    rand2.setSibling(null);

    valueStack.push(rand1);
  }

  // Method to apply unary operations
  private boolean apply_UnaryOperation(ASTNode rator) {
    switch (rator.getType()) {
      case NOT:
        not();
        return true;
      case NEG:
        neg();
        return true;
      default:
        return false;
    }
  }

  private void not() {
    ASTNode rand = valueStack.pop();
    if (rand.getType() != ASTNodeType.TRUE && rand.getType() != ASTNodeType.FALSE)
      SyntaxError.printError(rand.getSourceLineNumber(),
          "Expecting a Boolean Value; was given \"" + rand.getValue() + "\"");

    if (rand.getType() == ASTNodeType.TRUE)
      False_Push();
    else
      True_Push();
  }

  private void neg() {
    ASTNode rand = valueStack.pop();
    if (rand.getType() != ASTNodeType.INTEGER)
      SyntaxError.printError(rand.getSourceLineNumber(),
          "Expecting a Boolean Value; was given \"" + rand.getValue() + "\"");

    ASTNode result = new ASTNode();
    result.setType(ASTNodeType.INTEGER);
    result.setValue(Integer.toString(-1 * Integer.parseInt(rand.getValue())));
    valueStack.push(result);
  }

  // Method to apply rand to rator
  private void applyRandToRator(ASTNode node, Delta currentDelta, Environment environment,
      Stack<ASTNode> stack) {
    // Pop the rator and rand from the value stack
    ASTNode rator = valueStack.pop();
    ASTNode rand = valueStack.pop();
    
    // Check if rator is a DELTA node
    if (rator.getType() == ASTNodeType.DELTA) {
      Delta nextDelta = (Delta) rator;
      
      // Create a new environment with the parent set to the running environment of nextDelta
      Environment newEnv = new Environment();
      newEnv.setParent(nextDelta.getRunningEnv());

      // Check if the number of bound variables in nextDelta is 1
      if (nextDelta.getBoundVars().size() == 1) {
        newEnv.addMapping(nextDelta.getBoundVars().get(0), rand);
      }
      // RULE 11 
      else {
        if (rand.getType() != ASTNodeType.TUPLE)
          SyntaxError.printError(rand.getSourceLineNumber(),
              "Expected a tuple; was given \"" + rand.getValue() + "\"");

        for (int i = 0; i < nextDelta.getBoundVars().size(); i++) {
          newEnv.addMapping(nextDelta.getBoundVars().get(i), Nth_Tuple_Child((Tuple) rand, i + 1)); // + 1 coz tuple
                                                                                                    // indexing starts
                                                                                                    // at 1
        }
      }
      
      // Process the control stack with the new environment
      processControlStack(nextDelta, newEnv);
      return;
    } else if (rator.getType() == ASTNodeType.YSTAR) {
      // RULE 12
      if (rand.getType() != ASTNodeType.DELTA)
        SyntaxError.printError(rand.getSourceLineNumber(),
            "Expected a Delta; was given \"" + rand.getValue() + "\"");

      Eta etaNode = new Eta();
      etaNode.setDelta((Delta) rand);
      valueStack.push(etaNode);
      return;
    } else if (rator.getType() == ASTNodeType.ETA) {
      // If rator is an ETA node, push back rand, rator, and the delta it contains
        // Then push back two gammas (one for the eta and one for the delta) (RULE 13)
      valueStack.push(rand);
      valueStack.push(rator);
      valueStack.push(((Eta) rator).getDelta());
      // push back two gammas (one for the eta and one for the delta)
      stack.push(node);
      stack.push(node);
      return;
    } else if (rator.getType() == ASTNodeType.TUPLE) {
      // If rator is a TUPLE node, perform tuple selection
      Tuple_Selection((Tuple) rator, rand);
      return;
    } else if (evaluate_BuilIn_Functions(rator, rand, stack))
      return;
    else
      SyntaxError.printError(rator.getSourceLineNumber(),
          "Cannot evaluate \"" + rator.getValue() + "\"");
  }
  

  // Method to evaluate built-in function
  private boolean evaluate_BuilIn_Functions(ASTNode rator, ASTNode rand, Stack<ASTNode> stack) {
    switch (rator.getValue()) {
      // Check if rator is a built-in function and perform the corresponding operation
      case "Isinteger":
        pushNode_By_BoolValue(rand, ASTNodeType.INTEGER);
        return true;
      case "Isstring":
        pushNode_By_BoolValue(rand, ASTNodeType.STRING);
        return true;
      case "Isdummy":
        pushNode_By_BoolValue(rand, ASTNodeType.DUMMY);
        return true;
      case "Isfunction":
        pushNode_By_BoolValue(rand, ASTNodeType.DELTA);
        return true;
      case "Istuple":
        pushNode_By_BoolValue(rand, ASTNodeType.TUPLE);
        return true;
      case "Istruthvalue":
        if (rand.getType() == ASTNodeType.TRUE || rand.getType() == ASTNodeType.FALSE)
          True_Push();
        else
          False_Push();
        return true;
      case "Stem":
        stem(rand);
        return true;
      case "Stern":
        stern(rand);
        return true;
      case "Conc":
      case "conc":
        conc(rand, stack);
        return true;
      case "Print":
      case "print":
        get_Node_Value(rand);
        push_DummyNode();
        return true;
      case "ItoS":
        int_To_str(rand);
        return true;
      case "Order":
        order(rand);
        return true;
      case "Null":
        is_Empty_Tuple(rand);
        return true;
      default:
        return false;
    }
  }
  
  // Method to push a node onto the value stack based on its boolean value
  private void pushNode_By_BoolValue(ASTNode rand, ASTNodeType type) {
    if (rand.getType() == type)
      True_Push();
    else
      False_Push();
  }
  
  // Method to push a TRUE node onto the value stack
  private void True_Push() {
    ASTNode trueNode = new ASTNode();
    trueNode.setType(ASTNodeType.TRUE);
    trueNode.setValue("true");
    valueStack.push(trueNode);
  }
  
  // Method to push a FALSE node onto the value stack
  private void False_Push() {
    ASTNode falseNode = new ASTNode();
    falseNode.setType(ASTNodeType.FALSE);
    falseNode.setValue("false");
    valueStack.push(falseNode);
  }
  
  // Method to push a DUMMY node onto the value stack
  private void push_DummyNode() {
    ASTNode falseNode = new ASTNode();
    falseNode.setType(ASTNodeType.DUMMY);
    valueStack.push(falseNode);
  }
  
  // Method to extract the first character of a string
  private void stem(ASTNode rand) {
    if (rand.getType() != ASTNodeType.STRING)
      SyntaxError.printError(rand.getSourceLineNumber(),
          "Expected a string; was given \"" + rand.getValue() + "\"");

    if (rand.getValue().isEmpty())
      rand.setValue("");
    else
      rand.setValue(rand.getValue().substring(0, 1));

    valueStack.push(rand);
  }
  
  // Method to extract all but the first character of a string
  private void stern(ASTNode rand) {
    if (rand.getType() != ASTNodeType.STRING)
      SyntaxError.printError(rand.getSourceLineNumber(),
          "Expected a string; was given \"" + rand.getValue() + "\"");

    if (rand.getValue().isEmpty() || rand.getValue().length() == 1)
      rand.setValue("");
    else
      rand.setValue(rand.getValue().substring(1));

    valueStack.push(rand);
  }
  
  // Method to concatenate two strings
  private void conc(ASTNode rand1, Stack<ASTNode> currentControlStack) {
    currentControlStack.pop();
    ASTNode rand2 = valueStack.pop();
    if (rand1.getType() != ASTNodeType.STRING || rand2.getType() != ASTNodeType.STRING)
      SyntaxError.printError(rand1.getSourceLineNumber(),
          "Expected two strings; was given \"" + rand1.getValue() + "\", \"" + rand2.getValue() + "\"");

    ASTNode result = new ASTNode();
    result.setType(ASTNodeType.STRING);
    result.setValue(rand1.getValue() + rand2.getValue());

    valueStack.push(result);
  }
  
  
// Method to convert an integer to a string
  private void int_To_str(ASTNode rand) {
    if (rand.getType() != ASTNodeType.INTEGER)
      SyntaxError.printError(rand.getSourceLineNumber(),
          "Expected an integer; was given \"" + rand.getValue() + "\"");

    rand.setType(ASTNodeType.STRING);
    valueStack.push(rand);
  }
  
  // Method to calculate the order of a tuple
  private void order(ASTNode rand) {
    if (rand.getType() != ASTNodeType.TUPLE)
      SyntaxError.printError(rand.getSourceLineNumber(), "Expected a tuple; was given \"" + rand.getValue() + "\"");

    ASTNode result = new ASTNode();
    result.setType(ASTNodeType.INTEGER);
    result.setValue(Integer.toString(countChildren(rand)));

    valueStack.push(result);
  }
  
  
// Method to check if a tuple is empty
  private void is_Empty_Tuple(ASTNode rand) {
    if (rand.getType() != ASTNodeType.TUPLE)
      SyntaxError.printError(rand.getSourceLineNumber(), "Expected a tuple; was given \"" + rand.getValue() + "\"");

    if (countChildren(rand) == 0)
      True_Push();
    else
      False_Push();
  }

  // RULE 10 (// Method to perform tuple selection)
  private void Tuple_Selection(Tuple rator, ASTNode rand) {
    if (rand.getType() != ASTNodeType.INTEGER)
      SyntaxError.printError(rand.getSourceLineNumber(),
          "tuple index must be Integer, Not with \"" + rand.getValue() + "\"");

    ASTNode result = Nth_Tuple_Child(rator, Integer.parseInt(rand.getValue()));
    if (result == null)
      SyntaxError.printError(rand.getSourceLineNumber(),
          "Tuple  index " + rand.getValue() + " out of bounds");

    valueStack.push(result);
  }
  
  // Method to retrieve the nth child of a tuple
  private ASTNode Nth_Tuple_Child(Tuple TAU_Node, int index) {
    ASTNode childNode = TAU_Node.getChild();
    for (int i = 1; i < index; ++i) { // tuple selection index starts at 1
      if (childNode == null)
        break;
      childNode = childNode.getSibling();
    }
    return childNode;
  }
  
  // Method to handle identifiers
  private void handle_Identifiers(ASTNode node, Environment currentEnv) {
    if (currentEnv.lookup(node.getValue()) != null) // RULE 1
      valueStack.push(currentEnv.lookup(node.getValue()));
    else if (isReserved_Identifier(node.getValue()))
      valueStack.push(node);
    else
      SyntaxError.printError(node.getSourceLineNumber(), "identifier is not declared\"" + node.getValue() + "\"");
  }

  // Method to evaluate TAU node (RULE 9)
  private void evaluateTAU_Node(ASTNode node) {
    int numChildren = countChildren(node);
    Tuple tupleNode = new Tuple();
    if (numChildren == 0) {
      valueStack.push(tupleNode);
      return;
    }

    ASTNode childNode = null, tempNode = null;
    for (int i = 0; i < numChildren; ++i) {
      if (childNode == null)
        childNode = valueStack.pop();
      else if (tempNode == null) {
        tempNode = valueStack.pop();
        childNode.setSibling(tempNode);
      } else {
        tempNode.setSibling(valueStack.pop());
        tempNode = tempNode.getSibling();
      }
    }
    tempNode.setSibling(null);
    tupleNode.setChild(childNode);
    valueStack.push(tupleNode);
  }

  // Method to evaluate BETA node (RULE 8)
  private void evaluate_BetaNode(Beta node, Stack<ASTNode> stack) {
    ASTNode conditionResultNode = valueStack.pop();

    if (conditionResultNode.getType() != ASTNodeType.TRUE && conditionResultNode.getType() != ASTNodeType.FALSE)
      SyntaxError.printError(conditionResultNode.getSourceLineNumber(),
          "Expecting a truthvalue; found \"" + conditionResultNode.getValue() + "\"");

    if (conditionResultNode.getType() == ASTNodeType.TRUE)
      stack.addAll(node.getTHEN());
    else
      stack.addAll(node.getELSE());
  }
  
  // Method to count the number of children of a node
  private int countChildren(ASTNode node) {
    int numChildren = 0;
    ASTNode childNode = node.getChild();
    while (childNode != null) {
      numChildren++;
      childNode = childNode.getSibling();
    }
    return numChildren;
  }
  
  // Method to get the value of a node
  private void get_Node_Value(ASTNode rand) {
    String evaluationResult = rand.getValue();
    evaluationResult = evaluationResult.replace("\\t", "\t");
    evaluationResult = evaluationResult.replace("\\n", "\n");
    System.out.print(evaluationResult);
  }

 // Method to check if an identifier is a reserved keyword
  private boolean isReserved_Identifier(String value) {
    switch (value) {
      case "Isinteger":
      case "Isstring":
      case "Istuple":
      case "Isdummy":
      case "Istruthvalue":
      case "Isfunction":
      case "ItoS":
      case "Order":
      case "Conc":
      case "conc":
      case "Stern":
      case "Stem":
      case "Null":
      case "Print":
      case "print":
      case "neg":
        return true;
    }
    return false;
  }

}
