import kotlin.test.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree

import java.io.FileInputStream
import java.io.InputStream

import hapi.*
  
class VisitorTest {
  
    @Test
    @DisplayName("Should generate the correct IR")
    fun shouldGenerateTheCorrectIR() {
      val file = "src/test/fixtures/visitor/Main.hp"
      val priority = listOf("Actors", "Actions", "Resources")

      val ir = (IR.generate(file, priority) as IRNode).ir

      val validIRString = "{Bob={Updates=[SSN], Deletes=[SSN], Reads=[SSN]}, Alice={Updates=[SSN, CCN], Deletes=[SSN, EMAIL, CCN], Reads=[SSN, EMAIL, CCN]}}"

      assertThat(ir.toString()).isEqualTo(validIRString)
    }

    @Test
    @DisplayName("Should receive wrong export name error")
    fun shouldReceiveWrongNameError() {
      val file = "src/test/fixtures/wrong-name/Main.hp"
      val priority = listOf("Actors", "Actions", "Resources")
      val exception = assertFailsWith<Exception> { IR.generate(file, priority) }
      assertEquals("undefined name: WrongName::bob", exception.message)

    }
}