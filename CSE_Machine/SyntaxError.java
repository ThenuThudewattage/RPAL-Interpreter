package CSE_Machine;

// SyntaxError class handles printing error messages for syntax errors in the CSE machine
public class SyntaxError {
  
  // Method to print an error message with the source line number and exit the program
  public static void printError(int sourceLineNumber, String message) {
    System.out.println(":" + sourceLineNumber + ": " + message);
    System.exit(1);
  }

}
