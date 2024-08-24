import CSE_Machine.*;
import PARSER.*;
import SCANNER.*;
import java.io.IOException;

public class myrpal {
  public static String fileName;

  public static void main(String[] args) throws Exception {
    fileName = ""; // = args[0];
    AST ast = null;
    boolean astFlag = false;

    if (args.length < 1) {
      System.out.println("Usage: java myrpal -ast <filename>");
      return;
    }

    for (String arg : args) {
      System.out.println(arg);
    }

    if (args[0].equals("-ast")) {
      if (args.length < 2) {
        System.out.println("Error: Missing filename after -ast flag");
        return;
      }
      astFlag = true;
      fileName = args[1];
    } else
      fileName = args[0];

    try {
      Scanner scanner = new Scanner(fileName);
      Parser parser = new Parser(scanner);
      ast = parser.buildAST();
    } catch (IOException e) {
      throw new ParseException("ERROR: File cannot be read, please check again. ");
    }

    if (astFlag) {
      ast.printAST(); // if ast flag is there then print the AST, nothing else
      /*
       * as for the requirements,
       * Required switches: -ast. This switch prints the abstract syntax tree, and
       * nothing else.
       */
    } else {
      ast.standardize();
      CSEMachine csem = new CSEMachine(ast);
      csem.evaluateProgram();
      System.out.println();
    }
  }
}
