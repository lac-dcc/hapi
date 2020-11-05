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

    val ir = irFromString(program, listOf("Prop"))

    assertThat(ir.toString()).isEqualTo(expected)
  }

  @Test
  @DisplayName("Should throw undefined value and attribute error")
  fun shouldThrowUndefinedError() {
    val program = { attr: String ->
      """
      data P = P1;
      data K = K1;
      main =  
        DENY
        EXCEPT {
          ALLOW {
            P: P1
            ${attr}
          }
        };
      """
    }


    val exception = assertFailsWith<Exception>{ irFromString(programs.get(i), listOf("Prop")) }
    assertEquals(messages.get(i), exception.message)

    val ir1 = irFromString(program("K: K1"))
    val ir2 = irFromString(program("E: E1"))

    assertThat(ir1.toString()).isEqualTo(e1)
    assertThat(ir2.toString()).isEqualTo(e2)
  }
}