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

    @Test
    @DisplayName("Should return a null pointer Error") // kotlin.KotlinNullPointerException
    fun shouldReturnSyntaxError1() {
      val file = "src/test/fixtures/syntax-error/error-1/Main.hp"
      val priority = listOf("Actors", "Actions", "Resources")
      val exception = assertFailsWith<Exception> { IR.generate(file, priority) }
      assertEquals(null, exception.message)
    }

    @Test
    @DisplayName("Should return a Syntax Error") // Or Should DENY Alice Reads CCN
    fun shouldReturnSyntaxError2() {
      val file = "src/test/fixtures/syntax-error/error-2/Main.hp"
      val priority = listOf("Actors", "Actions", "Resources")
      //val exception = assertFailsWith<Exception> { IR.generate(file, priority) }
      //assertEquals("Some syntax error", exception.message)
      val ir = (IR.generate(file, priority) as IRNode).ir
      val expectedIRString = "{Bob={Updates=[SSN, EMAIL, CCN], Deletes=[SSN, EMAIL, CCN], Reads=[SSN, EMAIL, CCN]}, Alice={Updates=[SSN, EMAIL, CCN], Deletes=[SSN, EMAIL, CCN], Reads=[SSN, EMAIL]}}"
      assertThat(ir.toString()).isEqualTo(expectedIRString)

    }

    @Test
    @DisplayName("Should DENY Alice Reads CCN") // Or Should return a Syntax Error
    fun shouldReturnSyntaxError3() {
      val file = "src/test/fixtures/syntax-error/error-3/Main.hp"
      val priority = listOf("Actors", "Actions", "Resources")
      val ir = (IR.generate(file, priority) as IRNode).ir
      val expectedIRString = "{Bob={Updates=[SSN, EMAIL, CCN], Deletes=[SSN, EMAIL, CCN], Reads=[SSN, EMAIL, CCN]}, Alice={Updates=[SSN, EMAIL, CCN], Deletes=[SSN, EMAIL, CCN], Reads=[SSN, EMAIL]}}"
      //print(ir.toString())
      assertThat(ir.toString()).isEqualTo(expectedIRString)
      
    }

    @Test
    @DisplayName("Should DENY Bob Actions on EMAIL")
    fun shouldReturnSyntaxError4() {
      val file = "src/test/fixtures/syntax-error/error-4/Main.hp"
      val priority = listOf("Actors", "Actions", "Resources")
      val ir = (IR.generate(file, priority) as IRNode).ir
      val expectedIRString = "{Bob={Updates=[SSN, CCN], Deletes=[SSN, CCN], Reads=[SSN, CCN]}, Alice={Updates=[SSN, EMAIL, CCN], Deletes=[SSN, EMAIL, CCN], Reads=[SSN, EMAIL, CCN]}}"
      assertThat(ir.toString()).isEqualTo(expectedIRString)
    }
}