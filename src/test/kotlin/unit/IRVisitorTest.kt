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
    val priority = listOf("Actors", "Actions", "Resources")

    val expected = "{Bob={Updates=[SSN], Deletes=[SSN], Reads=[SSN]}, Alice={Updates=[SSN, CCN], Deletes=[SSN, EMAIL, CCN], Reads=[SSN, EMAIL, CCN]}}"

    val ir = irFromFile(file, priority)

    assertThat(ir.toString()).isEqualTo(expected)
  }

  @Test
  @DisplayName("Should receive name error when exporting a module different of filename")
  fun shouldReceiveWrongNameError() {
    val file = "src/test/fixtures/wrong-name/Main.hp"
    val priority = listOf("Actors", "Actions", "Resources")
    
    val exception = assertFailsWith<Exception> { irFromFile(file, priority) }
    assertEquals("undefined name: WrongName::bob", exception.message)
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
      "expected type ALLOW got DENY",
      "expected type DENY got ALLOW",
      "expected type ALLOW got DENY"
    )
    for (i in 0 until programs.size){
      val exception = assertFailsWith<Exception>{ irFromString(programs.get(i), listOf("Prop")) }
      assertEquals(messages.get(i), exception.message)
    }

    @Test
    @DisplayName("Should receive an undefined attribute error")
    fun shouldReceiveUndefinedAttributeError() {
      val file = "src/test/fixtures/wrong-name/undefined-attribute/Main.hp"
      val priority = listOf("Actors", "Actions", "Resources")
      val exception = assertFailsWith<Exception> { irFromFile(file, priority) }
      assertEquals("undefined attribute: Syntax::jeff::Actors = Jefa", exception.message)
    }

    @Test
    @DisplayName("Should return a syntax error on imported variable: DENY after an ALLOW")
    fun shouldReturnSyntaxErrorImportedVariable() {
      val file = "src/test/fixtures/syntax-error/imported-variable/Main.hp"
      val priority = listOf("Actors", "Actions", "Resources")
      
      val exception = assertFailsWith<Exception> { 
        irFromFile(file, priority)
      }

      assertEquals("line 29:10 syntax error 'DENY' after an 'ALLOW'", exception.message)

    }

    @Test
    @DisplayName("Should generate the correct IR even with multiple 'EXCEPT' nested")
    fun shouldDenyBobActionsOnEmail() {
      val file = "src/test/fixtures/visitor/nested-excepts/Main.hp"
      val priority = listOf("Actors", "Actions", "Resources")
      val ir = irFromFile(file, priority)
      val expectedIRString = "{Bob={Updates=[SSN, CCN], Deletes=[SSN, CCN], Reads=[SSN, CCN]}, Alice={Updates=[SSN, EMAIL, CCN], Deletes=[SSN, EMAIL, CCN], Reads=[SSN, EMAIL, CCN]}}"
      assertThat(ir.toString()).isEqualTo(expectedIRString)
    }
}