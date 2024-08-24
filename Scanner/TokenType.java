package SCANNER;

// TokenType enum represents the types of tokens recognized by the scanner
public enum TokenType {
    IDENTIFIER, // Token representing an identifier (e.g., variable name)
    INTEGER, // Token representing an integer value
    STRING, // Token representing a string value
    OPERATOR, // Token representing an operator symbol
    DELETE, // Token representing a space or comment that should be deleted (whitespace, comments)
    L_PAREN, // Token representing a left parenthesis "("
    R_PAREN, // Token representing a right parenthesis ")"
    SEMICOLON, // Token representing a semicolon ";"
    COMMA, // Token representing a comma ","
    KEYWORD; // Token representing a keyword (e.g., reserved identifiers like "let", "in", "fn")
}
