import kotlin.test.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree

import hapi.*
  
class ParsingTest {
  
    @Test
    @DisplayName("Should parse infinitely nested clauses")
    fun shouldParseInfinitelyNestedClauses(){
      val program =
        """
        data Prop = P1, P2, P3;
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
              }
            }
          };
        """
      assertNotNull(parseString(program))
    }
}