package SCANNER;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class Scanner {
  private BufferedReader buffer;
  private String extraCharRead;
  private final List<String> reservedIdentifiers = Arrays
      .asList(new String[] { "let", "in", "within", "fn", "where", "aug", "or",
          "not", "gr", "ge", "ls", "le", "eq", "ne", "true",
          "false", "nil", "dummy", "rec", "and" });
  private int sourceLineNumber;
  
  // Constructor to initialize the scanner with the input file
  public Scanner(String inputFile) throws IOException {
    sourceLineNumber = 1;
    buffer = new BufferedReader(new InputStreamReader(new FileInputStream(new File(inputFile))));
  }
  
  // Method to read the next token from the input file
  public Token readNextToken() {
    Token nextToken = null;
    String nextChar;
    if (extraCharRead != null) {
      nextChar = extraCharRead;
      extraCharRead = null;
    } else
      nextChar = readNextChar();
    if (nextChar != null)
      nextToken = buildToken(nextChar);
    return nextToken;
  }
  
  // Method to read the next character from the input file
  private String readNextChar() {
    String nextChar = null;
    try {
      int c = buffer.read();
      if (c != -1) {
        nextChar = Character.toString((char) c);
        if (nextChar.equals("\n"))
          sourceLineNumber++;
      } else
        buffer.close();
    } catch (IOException e) {
    }
    return nextChar;
  }
  

  // Method to build the token based on the current character read
  private Token buildToken(String currentChar) {
    Token nextToken = null;
    if (LexicalRegexPatterns.LetterPattern.matcher(currentChar).matches()) {
      nextToken = buildIdentifierToken(currentChar);
    } else if (LexicalRegexPatterns.DigitPattern.matcher(currentChar).matches()) {
      nextToken = buildIntegerToken(currentChar);
    } else if (LexicalRegexPatterns.OpSymbolPattern.matcher(currentChar).matches()) { // comment tokens are also entered
                                                                                      // from here
      nextToken = buildOperatorToken(currentChar);
    } else if (currentChar.equals("\'")) {
      nextToken = buildStringToken(currentChar);
    } else if (LexicalRegexPatterns.SpacePattern.matcher(currentChar).matches()) {
      nextToken = buildSpaceToken(currentChar);
    } else if (LexicalRegexPatterns.PunctuationPattern.matcher(currentChar).matches()) {
      nextToken = buildPunctuationPattern(currentChar);
    }
    return nextToken;
  }
  
  // Method to build an identifier token
  private Token buildIdentifierToken(String currentChar) {
    Token identifierToken = new Token();
    identifierToken.setType(TokenType.IDENTIFIER);
    identifierToken.setSourceLineNumber(sourceLineNumber);
    StringBuilder sBuilder = new StringBuilder(currentChar);

    String nextChar = readNextChar();
    while (nextChar != null) { 
      if (LexicalRegexPatterns.IdentifierPattern.matcher(nextChar).matches()) {
        sBuilder.append(nextChar);
        nextChar = readNextChar();
      } else {
        extraCharRead = nextChar;
        break;
      }
    }

    String value = sBuilder.toString();
    if (reservedIdentifiers.contains(value))
      identifierToken.setType(TokenType.KEYWORD);

    identifierToken.setValue(value);
    return identifierToken;
  }
  
  // Method to build an integer token
  private Token buildIntegerToken(String currentChar) {
    Token integerToken = new Token();
    integerToken.setType(TokenType.INTEGER);
    integerToken.setSourceLineNumber(sourceLineNumber);
    StringBuilder sBuilder = new StringBuilder(currentChar);

    String nextChar = readNextChar();
    while (nextChar != null) { // null indicates the file ended
      if (LexicalRegexPatterns.DigitPattern.matcher(nextChar).matches()) {
        sBuilder.append(nextChar);
        nextChar = readNextChar();
      } else {
        extraCharRead = nextChar;
        break;
      }
    }

    integerToken.setValue(sBuilder.toString());
    return integerToken;
  }

  // Method to build an operator token
  private Token buildOperatorToken(String currentChar) {
    Token opSymbolToken = new Token();
    opSymbolToken.setType(TokenType.OPERATOR);
    opSymbolToken.setSourceLineNumber(sourceLineNumber);
    StringBuilder sBuilder = new StringBuilder(currentChar);

    String nextChar = readNextChar();

    if (currentChar.equals("/") && nextChar.equals("/"))
      return buildCommentToken(currentChar + nextChar);

    while (nextChar != null) { // null indicates the file ended
      if (LexicalRegexPatterns.OpSymbolPattern.matcher(nextChar).matches()) {
        sBuilder.append(nextChar);
        nextChar = readNextChar();
      } else {
        extraCharRead = nextChar;
        break;
      }
    }

    opSymbolToken.setValue(sBuilder.toString());
    return opSymbolToken;
  }
  
  // Method to build a string token
  private Token buildStringToken(String currentChar) {
    Token stringToken = new Token();
    stringToken.setType(TokenType.STRING);
    stringToken.setSourceLineNumber(sourceLineNumber);
    StringBuilder sBuilder = new StringBuilder("");

    String nextChar = readNextChar();
    while (nextChar != null) { 
      if (nextChar.equals("\'")) { 
        
        stringToken.setValue(sBuilder.toString());
        return stringToken;
      } else if (LexicalRegexPatterns.StringPattern.matcher(nextChar).matches()) { // match Letter | Digit |
                                                                                   // Operator_symbol
        sBuilder.append(nextChar);
        nextChar = readNextChar();
      }
    }

    return null;
  }
  // Method to build a space token
  private Token buildSpaceToken(String currentChar) {
    Token deleteToken = new Token();
    deleteToken.setType(TokenType.DELETE);
    deleteToken.setSourceLineNumber(sourceLineNumber);
    StringBuilder sBuilder = new StringBuilder(currentChar);

    String nextChar = readNextChar();
    while (nextChar != null) { // null indicates the file ended
      if (LexicalRegexPatterns.SpacePattern.matcher(nextChar).matches()) {
        sBuilder.append(nextChar);
        nextChar = readNextChar();
      } else {
        extraCharRead = nextChar;
        break;
      }
    }

    deleteToken.setValue(sBuilder.toString());
    return deleteToken;
  }
  
  // Method to build a comment token
  private Token buildCommentToken(String currentChar) {
    Token commentToken = new Token();
    commentToken.setType(TokenType.DELETE);
    commentToken.setSourceLineNumber(sourceLineNumber);
    StringBuilder sBuilder = new StringBuilder(currentChar);

    String nextChar = readNextChar();
    while (nextChar != null) { // null indicates the file ended
      if (LexicalRegexPatterns.CommentPattern.matcher(nextChar).matches()) {
        sBuilder.append(nextChar);
        nextChar = readNextChar();
      } else if (nextChar.equals("\n"))
        break;
    }

    commentToken.setValue(sBuilder.toString());
    return commentToken;
  }
  // Method to build a punctuation token
  private Token buildPunctuationPattern(String currentChar) {
    Token punctuationToken = new Token();
    punctuationToken.setSourceLineNumber(sourceLineNumber);
    punctuationToken.setValue(currentChar);
    if (currentChar.equals("("))
      punctuationToken.setType(TokenType.L_PAREN);
    else if (currentChar.equals(")"))
      punctuationToken.setType(TokenType.R_PAREN);
    else if (currentChar.equals(";"))
      punctuationToken.setType(TokenType.SEMICOLON);
    else if (currentChar.equals(","))
      punctuationToken.setType(TokenType.COMMA);

    return punctuationToken;
  }
}
