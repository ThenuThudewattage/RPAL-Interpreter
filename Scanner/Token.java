package SCANNER;

// Token class represents a lexical token identified by the scanner
public class Token {
    private TokenType type; // Type of the token
    private String value; // Value of the token (e.g., identifier name, integer value)
    private int sourceLineNumber; // Line number in the source file where the token was found

    // Method to get the type of the token
    public TokenType getType() {
        return this.type;
    }

    // Method to set the type of the token
    public void setType(TokenType type) {
        this.type = type;
    }

    // Method to get the value of the token
    public String getValue() {
        return this.value;
    }

    // Method to set the value of the token
    public void setValue(String value) {
        this.value = value;
    }

    // Method to get the source line number of the token
    public int getSourceLineNumber() {
        return this.sourceLineNumber;
    }

    // Method to set the source line number of the token
    public void setSourceLineNumber(int sourceLineNumber) {
        this.sourceLineNumber = sourceLineNumber;
    }
}
