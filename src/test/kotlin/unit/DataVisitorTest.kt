import kotlin.test.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree

import hapi.*
  
class DataVisitorTest {
  
    @Test
    @DisplayName("Should generate the correct DataMap")
    fun shouldGenerateTheCorrectDataMap() {
      val program =
        """
        data Actors = 
        Looker(Analyst),
        Analyst(Alice, Bob),
        Intern(Bob, Jeff),
        Alice, Bob, Jeff;
        """
      val expected = "{Actors={Top=[Looker, Intern], Looker=[Analyst], Analyst=[Alice, Bob], Alice=[], Bob=[], Intern=[Bob, Jeff], Jeff=[]}}"
      
      evalDataMap(program, "").let {
        assertThat(it.toString()).isEqualTo(expected)
      }
    }
}