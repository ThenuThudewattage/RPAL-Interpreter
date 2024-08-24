package SCANNER;

import java.util.regex.Pattern;

// LexicalRegexPatterns class to define regular expressions for lexical analysis
public class LexicalRegexPatterns {

  // Regular expression strings for different lexical elements
  private static final String letterRegexString = "a-zA-Z";
  private static final String digitRegexString = "\\d";
  private static final String spaceRegexString = "[\\s\\t\\n]";
  private static final String punctuationRegexString = "();,";
  private static final String opSymbolRegexString = "+-/~:=|!#%_{}\"*<>.&$^\\[\\]?@";
  private static final String opSymbolToEscapeString = "([*<>.&$^?])";

  public static final Pattern LetterPattern = Pattern.compile("[" + letterRegexString + "]");

  public static final Pattern IdentifierPattern = Pattern.compile("[" + letterRegexString + digitRegexString + "_]");

  public static final Pattern DigitPattern = Pattern.compile(digitRegexString);

  public static final Pattern PunctuationPattern = Pattern.compile("[" + punctuationRegexString + "]");

  public static final String opSymbolRegex = "[" + escapeMetaChars(opSymbolRegexString, opSymbolToEscapeString) + "]";
  public static final Pattern OpSymbolPattern = Pattern.compile(opSymbolRegex);

  public static final Pattern StringPattern = Pattern.compile("[ \\t\\n\\\\" + punctuationRegexString
      + letterRegexString + digitRegexString + escapeMetaChars(opSymbolRegexString, opSymbolToEscapeString) + "]");

  public static final Pattern SpacePattern = Pattern.compile(spaceRegexString);

  public static final Pattern CommentPattern = Pattern.compile("[ \\t\\'\\\\ \\r" + punctuationRegexString
      + letterRegexString + digitRegexString + escapeMetaChars(opSymbolRegexString, opSymbolToEscapeString) + "]"); 

  private static String escapeMetaChars(String inputString, String charsToEscape) { // Method to escape meta characters in the regular expression
    return inputString.replaceAll(charsToEscape, "\\\\\\\\$1");
  }
}