import kotlin.test.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.misc.ParseCancellationException

import hapi.*

class ParsingTest {
  
  @Test
  @DisplayName("Should parse infinitely nested clauses")
  fun shouldParseInfinitelyNestedClauses(){
    val program =
      """
      data Prop = P1(P2), P2(P3, P4), P3, P4;
      main =  
        DENY
        EXCEPT {
          ALLOW {
            Prop: P1
          } EXCEPT {
            DENY {
              Prop: P2
            } EXCEPT {
              ALLOW {
                Prop: P3
              }
              ALLOW {
                Prop: P4
              }
            }
          }
        };
      """
    assertNotNull(parse(tokenize(program)))
  }

  @Test
  @DisplayName("Should throw ParseCancellationException on syntax error")
  fun shouldThrowParseCancellationExceptionOnSyntaxError(){
    val program =
      """
      data Prop = P1(P2), P2(P3, P4), P3, P4;
      main =  
        DENY
        EXCEPT {
          INVALID { // this is a syntax error
            Prop: P4 
          }
        };
      """
    val error = "line 6:10 mismatched input 'INVALID' expecting 'ALLOW'"
    assertFailsWith<ParseCancellationException>(error) { parse(tokenize(program)) }
  }
  @Test
  @DisplayName("Should parse valid attribute expressions")
  fun shouldParseValidAttributeExpression(){
    val program =
    """
      data Prop = P1;

      denyP1 = DENY {
        Prop: P1
      };
      main =
        ALLOW
        EXCEPT {
          denyP1
        };
    """
    assertNotNull(parse(tokenize(program)))
  }

  @Test
  @DisplayName("Invalid import statement - missing comma")
  fun invalidSyntaxMissingComma(){
    val program =
    """
      import somemodule

      data Prop = P1;

      denyP1 = DENY {
        Prop: P1
      };
      main =
        ALLOW
        EXCEPT {
          denyP1
        };
    """
    val error = "line 4:6 missing ';' at 'data'"
    val e1 = assertFailsWith<ParseCancellationException>{parse(tokenize(program))}
    assertEquals(error, e1.message)
  }

  @Test
  @DisplayName("Invalid EXCEPT - missing curly braces")
  fun invalidSyntaxMissingBraces(){
    val program =
    """
      data Prop = P1;
      main =
        ALLOW
        EXCEPT 
          denyP1
        };
    """
    val error = "line 6:10 missing '{' at 'denyP1'"
    val e1 = assertFailsWith<ParseCancellationException>{parse(tokenize(program))}
    assertEquals(error, e1.message)
  }
}