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
      
      val exception = assertFailsWith<Exception> { 
        irFromFile(file, priority)
      }

      assertEquals("undefined name: WrongName::bob", exception.message)
    }
}