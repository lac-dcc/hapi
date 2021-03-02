import kotlin.test.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import java.io.File

import helpers.*
import utils.DataMapOrderError

class YAMLTest {

  @Test
  @DisplayName("Should generate the correct YAML")
  fun shouldGenerateCorrectYAML() {
    val hapiFile = "src/test/fixtures/YAML/Main.hp"
    val output = File(yamlFromFile(hapiFile)).readText()

    val expectedFile = "src/test/fixtures/YAML/Expected.yaml"
    val expectedOutput = File(expectedFile).readText()

    assertThat(output).isEqualTo(expectedOutput)
  }
  
  @Test
  @DisplayName("Invalid DataMap names for YAML")
  fun invalidDataMapWrongNames() {
    val hapiFile = "src/test/fixtures/YAML/WrongNames.hp"

    val error = assertFailsWith<DataMapOrderError> {
      yamlFromFile(hapiFile)
    }
    
    val expectedMessage =
      "Expected data statements for [Actors, Actions, Resources]," +
      " got [Actors, Actions, Resource]"
    
    assertThat(error.message).isEqualTo(expectedMessage)
  }
  
  @Test
  @DisplayName("Invalid DataMap order for YAML")
  fun invalidDataMapWrongOrder() {
    val hapiFile = "src/test/fixtures/YAML/WrongOrder.hp"

    val error = assertFailsWith<DataMapOrderError> {
      yamlFromFile(hapiFile)
    }
    
    val expectedMessage =
      "Expected data statements for [Actors, Actions, Resources]," +
      " got [Actors, Resources, Actions]"
    
    assertThat(error.message).isEqualTo(expectedMessage)
  }
}
