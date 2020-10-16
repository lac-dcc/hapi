import kotlin.test.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree

import hapi.*
  
class IRVisitorTest {
  
    @Test
    @DisplayName("Should generate the correct IR")
    fun shouldGenerateTheCorrectIR() {
      val file = "src/test/fixtures/visitor/Main.hp"
      val priority = listOf("Actors", "Actions", "Resources")

      val datamap = genDataMap(file)
      val ir = (genIR(file, datamap, priority) as IRNode).ir

      val expected = "{Bob={Updates=[SSN], Deletes=[SSN], Reads=[SSN]}, Alice={Updates=[SSN, CCN], Deletes=[SSN, EMAIL, CCN], Reads=[SSN, EMAIL, CCN]}}"

      assertThat(ir.toString()).isEqualTo(expected)
    }

    @Test
    @DisplayName("Should receive name error when exporting a module different of filename")
    fun shouldReceiveWrongNameError() {
      val file = "src/test/fixtures/wrong-name/Main.hp"
      val priority = listOf("Actors", "Actions", "Resources")
      
      val exception = assertFailsWith<Exception> { 
        val datamap = genDataMap(file)
        genIR(file, datamap, priority)
      }

      assertEquals("undefined name: WrongName::bob", exception.message)
    }
}