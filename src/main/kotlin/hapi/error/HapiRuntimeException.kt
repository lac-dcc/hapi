package hapi.error

import java.lang.RuntimeException
import org.antlr.v4.runtime.tree.TerminalNode

class HapiRuntimeException(override val message: String): RuntimeException(message){

  companion object {
    operator fun invoke(token:TerminalNode , message: String): HapiRuntimeException {
      val charPosition = token.getSymbol().getCharPositionInLine()
      val line = token.getSymbol().getLine()
      val msg = "line " + line + ":" + charPosition + " " + message
      System.out.println(msg)
      return HapiRuntimeException(msg)
    }
  }
}