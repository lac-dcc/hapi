grammar Legalease;

tokens { INDENT, DEDENT }

@lexer::header {
  import org.antlr.v4.runtime.misc.Interval;
  import java.util.*;
}

@lexer::members {
  private Stack < Integer > indentLengths = new Stack < > ();
  private LinkedList < Token > pendingTokens = new LinkedList < > ();
  // An int that stores the last pending token type (including the inserted INDENT/DEDENT/NEWLINE token types also)
  private int lastPendingTokenType;
  private boolean wasSpaceIndentation = false;
  private boolean wasTabIndentation = false;
  private List < String > warnings = new ArrayList < > ();
  private List < String > errors = new ArrayList < > ();
  public static final String TEXT_LEXER = "lexer --> ";
  public static final String TEXT_INSERTED_INDENT = "inserted INDENT";
  
  @Override
  public Token nextToken() {
    if (_input.size() == 0) {
      return new CommonToken(EOF, "<EOF>"); // processing of the input stream until the first returning EOF
    } else {
      checkNextToken();
      return this.pendingTokens.pollFirst(); // append the token stream with the upcoming pending token
    }
  }
  
  private void checkNextToken() {
    if (this.indentLengths != null) { // after the first incoming EOF token the indentLengths stack will be set to null
      final int startSize = this.pendingTokens.size();
      Token curToken;
      do {
        curToken = super.nextToken();
        checkStartOfInput(curToken);
        switch (curToken.getType()) {
          case NEWLINE:
            handleNewLineToken(curToken);
            break;
          case EOF:
            handleEofToken(curToken); // indentLengths stack will be set to null
            break;
          default:
            this.pendingTokens.addLast(curToken);
        }
      } while (this.pendingTokens.size() == startSize);
      this.lastPendingTokenType = curToken.getType();
    }
  }
  
  private void checkStartOfInput(Token curToken) {
    if (indentLengths.size() == 0) {
      indentLengths.push(0);
      if (_input.getText(new Interval(0, 0)).trim().length() == 0) { // the first char of the input is a whitespace
        this.insertLeadingTokens(curToken.getType(), curToken.getStartIndex());
      }
    }
  }
  
  private void handleNewLineToken(Token newLineToken) {
    switch (_input.LA(1) /* next symbol */ ) {
      case '\r':
      case '\n':
      case '\f':
      case '#':
      case EOF: // skip the trailing inconsistent dedent or the trailing unexpected indent (or the trailing indent)
        return; // We're on a blank line or before a comment or before the EOF, skip the NEWLINE token
      default:
        Token nextToken = super.nextToken();
        int nextTokenType = nextToken.getType();
  
        if (nextTokenType == EXCEPT || nextTokenType == ALLOW || nextTokenType == DENY)
          this.pendingTokens.addLast(newLineToken);
  
        if (nextTokenType == ALLOW || nextTokenType == DENY)
          this.insertIndentDedentTokens(this.getIndentationLength(newLineToken.getText()));
  
        this.pendingTokens.addLast(nextToken);
    }
  }
  
  private void handleEofToken(Token curToken) {
    this.insertTrailingTokens(this.lastPendingTokenType); // indentLengths stack will be null!
    this.pendingTokens.addLast(curToken); // insert the current EOF token
    this.checkSpaceAndTabIndentation();
  }
  
  private void insertLeadingTokens(int type, int startIndex) {
    if (type != NEWLINE && type != EOF) { // (after a whitespace) The first token is visible, so We insert a NEWLINE and an INDENT token before it to raise an 'unexpected indent' error later by the parser
      this.insertToken(0, startIndex - 1, "<inserted leading NEWLINE>" + new String(new char[startIndex]).replace("\0", " "), NEWLINE, 1, 0);
      this.insertToken(startIndex, startIndex - 1, "<" + TEXT_INSERTED_INDENT + ", " + this.getIndentationDescription(startIndex) + ">", LegaleaseParser.INDENT, 1, startIndex);
      this.indentLengths.push(startIndex);
    }
  }
  
  private void insertIndentDedentTokens(int curIndentLength) {
    int prevIndentLength = this.indentLengths.peek();
    if (curIndentLength > prevIndentLength) { // insert an INDENT token
      this.insertToken("<" + TEXT_INSERTED_INDENT + ", " + this.getIndentationDescription(curIndentLength) + ">", LegaleaseParser.INDENT);
      this.indentLengths.push(curIndentLength);
    } else {
      while (curIndentLength < prevIndentLength) { // More than 1 DEDENT token may be inserted
        this.indentLengths.pop();
        prevIndentLength = this.indentLengths.peek();
        if (curIndentLength <= prevIndentLength) {
          this.insertToken("<inserted DEDENT, " + this.getIndentationDescription(prevIndentLength) + ">", LegaleaseParser.DEDENT);
        } else {
          this.insertToken("<inserted inconsistent DEDENT, " + "length=" + curIndentLength + ">", LegaleaseParser.DEDENT);
          this.errors.add(TEXT_LEXER + "line " + getLine() + ":" + getCharPositionInLine() + "\t IndentationError: unindent does not match any outer indentation level");
        }
      }
    }
  }
  
  private void insertTrailingTokens(int type) {
    if (type != NEWLINE && type != LegaleaseParser.DEDENT) {
      this.insertToken("<inserted trailing NEWLINE>", NEWLINE);
    }
  
    while (this.indentLengths.size() > 1) {
      this.insertToken("<inserted trailing DEDENT, " + this.getIndentationDescription(this.indentLengths.pop()) + ">", LegaleaseParser.DEDENT);
    }
  
    this.indentLengths = null; // there will be no more token read from the input stream
  }
  
  private String getIndentationDescription(int lengthOfIndent) {
    return "length=" + lengthOfIndent + ", level=" + this.indentLengths.size();
  }
  
  private void insertToken(String text, int type) {
    final int startIndex = _tokenStartCharIndex + getText().length();
    this.insertToken(startIndex, startIndex - 1, text, type, getLine(), getCharPositionInLine());
  }
  
  private void insertToken(int startIndex, int stopIndex, String text, int type, int line, int charPositionInLine) {
    CommonToken token = new CommonToken(_tokenFactorySourcePair, type, DEFAULT_TOKEN_CHANNEL, startIndex, stopIndex);
    token.setText(text);
    token.setLine(line);
    token.setCharPositionInLine(charPositionInLine);
    this.pendingTokens.addLast(token);
  }
  
  private int getIndentationLength(String textOfMatchedNEWLINE) {
    int count = 0;
    for (char ch: textOfMatchedNEWLINE.toCharArray()) {
      switch (ch) {
        case ' ':
          this.wasSpaceIndentation = true;
          count++;
          break;
        case '\t':
          this.wasTabIndentation = true;
          count += 8 - (count % 8);
          break;
      }
    }
    return count;
  }
  
  private void checkSpaceAndTabIndentation() {
    if (this.wasSpaceIndentation && this.wasTabIndentation) {
      this.warnings.add("Mixture of space and tab were used for indentation.");
    }
  }
  
  public List < String > getWarnings() {
    return this.warnings;
  }
  
  public List < String > getErrorMessages() {
    return this.errors;
  }
}

program: library | executable ;

library
  : importStmt* exportStmt
  ;

executable
  : importStmt* (dataStmt | letStmt)+
  ;

dataStmt: DATA ID '=' elem (',' elem)* ;

elem: ID ('(' value (',' value)* ')')? ;

letStmt: ID '=' NEWLINE? INDENT? policyExpr DEDENT? ;

exportStmt: EXPORT ID WHERE (dataStmt | letStmt)+;

importStmt: IMPORT ID ;

policyExpr: denyExpr | allowExpr ;

denyExpr
  : DENY exceptAllow NEWLINE?
  | DENY attributeExpr exceptAllow? NEWLINE? ;

allowExpr
  : ALLOW exceptDeny NEWLINE? 
  | ALLOW attributeExpr exceptDeny? NEWLINE? ;

exceptAllow: NEWLINE EXCEPT NEWLINE INDENT allowExpr+ DEDENT ;

exceptDeny: NEWLINE EXCEPT NEWLINE INDENT denyExpr+ DEDENT ;

attributeExpr
  : '{' attribute+ '}'
  | literalExpr
  ;

literalExpr: ID ('::' ID)? ;

attribute
  : ID '=' value (',' value)*
  | ID
  ;

value: ID;

DENY: 'DENY' ;
EXCEPT: 'EXCEPT' ;
ALLOW: 'ALLOW' ;
IMPORT: 'import' ;
EXPORT: 'export' ;
WHERE: 'where' ;
DATA: 'data' ;
ID:    [a-zA-Z0-9]+ ;

NEWLINE
 : ( '\r'? '\n' | '\r' | '\f' ) SPACES?
 ;

SKIP_
 : ( SPACES | COMMENT | LINE_JOINING ) -> skip
 ;

fragment SPACES
 : [ \t]+
 ;

fragment COMMENT
 : '#' ~[\r\n\f]*
 ;

fragment LINE_JOINING
 : '\\' SPACES? ( '\r'? '\n' | '\r' | '\f')
 ;
