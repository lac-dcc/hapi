grammar Hapi;

program
  : library 
  | executable 
  ;

library: exportStmt (stmt ';')+ ;

executable: (stmt ';')+ ;

stmt
  : letStmt
  | dataStmt
  | importStmt
  ;

exportStmt: EXPORT ID WHERE ;

importStmt: IMPORT ID ;

dataStmt: DATA ID '=' dataElem (',' dataElem)* ;

letStmt: ID '=' policyExpr ;

policyExpr
  : allowExpr
  | denyExpr
  | allowAllExceptExpr
  | denyAllExceptExpr
  ;

denyExpr
  : DENY attributeExpr
  | denyExceptExpr
  | literalExpr
  ;

denyExceptExpr: DENY attributeExpr EXCEPT '{' allowExpr+ '}';
denyAllExceptExpr: DENY EXCEPT '{' allowExpr+ '}';

allowExpr
  : ALLOW attributeExpr
  | allowExceptExpr
  | literalExpr
  ;
  
allowExceptExpr: ALLOW attributeExpr EXCEPT '{' denyExpr+ '}';
allowAllExceptExpr: ALLOW EXCEPT '{' denyExpr+ '}';

attributeExpr: '{' attribute+ '}' ;

literalExpr: ID ('::' ID)? ;

attribute
  : ID
  | ID ':' value (',' value)*
  ;

dataElem
  : ID
  | ID '(' value (',' value)* ')'
  ;

value: ID;

DENY: 'DENY' ;
EXCEPT: 'EXCEPT' ;
ALLOW: 'ALLOW' ;
IMPORT: 'import' ;
EXPORT: 'export' ;
WHERE: 'where' ;
DATA: 'data' ;
ID:    [a-zA-Z][_a-zA-Z0-9]* ;



SKIP_
  : ( NL | SPACES | COMMENT ) -> skip
  ;

fragment NL
  : ( '\r'? '\n' ) SPACES?
  ;

fragment SPACES
  : [ \t]+
  ;

fragment COMMENT
  : '//' ~[\r\n\f]*
  ;