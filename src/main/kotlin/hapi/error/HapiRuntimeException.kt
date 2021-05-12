package hapi.error

import java.lang.RuntimeException
import org.antlr.v4.runtime.tree.TerminalNode
import org.antlr.v4.runtime.Token

class HapiRuntimeException(override val message: String): RuntimeException(message){

  companion object {
    operator fun invoke(token:TerminalNode , message: String): HapiRuntimeException {
      return HapiRuntimeException(token.getSymbol(), message)
    }

    operator fun invoke(token:Token , message: String): HapiRuntimeException {
      val charPosition = token.getCharPositionInLine()
      val line = token.getLine()
      val msg = "line " + line + ":" + charPosition + " " + message
      System.out.println(msg)
      return HapiRuntimeException(msg)
    }
  }
}