import kotlin.test.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree

import hapi.*
import helpers.*
  
class EvaluationTest {
  
  @Test
  @DisplayName("Should evaluate to the correct IR even when defining Lattices after uses")
  fun shouldEvaluateCorrectly() {
    val program =
      """
      main =  
        DENY
        EXCEPT {
          ALLOW {
            Prop: P1
          }
        };

      data Prop = P1, P2;
      """
    val expected = "[P1]"

    val ir = irFromString(program)

    assertThat(ir.toString()).isEqualTo(expected)
  }

  @Test
  @DisplayName("Should consider the order of the definition of lattices when generating the IR")
  fun shouldConsiderOrder() {
    val program =
      """
      data P = P1;
      data K = K1;
      data E = E1;

      main =  
        DENY
        EXCEPT {
          ALLOW {
            P: P1
            K: K1
            E: E1
          }
        };

      """
    val expected = "{P1={K1=[E1]}}"

    val ir = irFromString(program)

    assertThat(ir.toString()).isEqualTo(expected)
  }
}