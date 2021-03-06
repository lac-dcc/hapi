import kotlin.test.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree

import helpers.*

class IRVisitorTest {
  
  @Test
  @DisplayName("Should generate the correct IR")
  fun shouldGenerateTheCorrectIR() {
    val file = "src/test/fixtures/visitor/Main.hp"

    val expected = "{Bob={Updates=[SSN], Deletes=[SSN], Reads=[SSN]}, Alice={Updates=[SSN, CCN], Deletes=[SSN, EMAIL, CCN], Reads=[SSN, EMAIL, CCN]}}"

    val ir = irFromFile(file)

    assertThat(ir.toString()).isEqualTo(expected)
  }

  @Test
  @DisplayName("Should receive name error when exporting a module different of filename")
  fun shouldReceiveWrongNameError() {
    val file = "src/test/fixtures/wrong-name/Main.hp"
    
    val exception = assertFailsWith<Exception> { irFromFile(file) }
    assertEquals("line 6:4 undefined name: WrongName::bob", exception.message)
  }

  @Test
  @DisplayName("Should throw error when using literal expressions of wrong type")
  fun shouldThrowErrorOnWrongLiteral() {
    val programs = listOf(
      """
      data Prop = P1;

      denyP1 = DENY {
        Prop: P1
      };
      main =
        DENY
        EXCEPT {
          denyP1 // use deny where allow is expected
        };
      """,
      """
      data Prop = P1;

      allowP1 = ALLOW {
        Prop: P1
      };
      main =
        ALLOW
        EXCEPT {
          allowP1 // use allow where deny is expected
        };
      """,
      """
      data Prop = P1, P2;

      allowP2 = ALLOW {
        Prop: P2
      };
      denyP1 = DENY {
        Prop: P1
      };
      main =
        DENY
        EXCEPT {
          allowP2
          denyP1 // use deny where allow is expected
          allowP2
        };
      """
    )
    val messages = listOf(
      "line 10:10 denyP1 is a DENY expression, expected ALLOW",
      "line 10:10 allowP1 is an ALLOW expression, expected DENY",
      "line 14:10 denyP1 is a DENY expression, expected ALLOW"
    )
    for (i in 0 until programs.size){
      val exception = assertFailsWith<Exception>{ irFromString(programs.get(i)) }
      assertEquals(messages.get(i), exception.message)
    }
  }
}