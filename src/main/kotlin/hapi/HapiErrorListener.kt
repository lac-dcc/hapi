package hapi

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime.tree.TerminalNode

/* 
create a decision map to manipulate errors that occurs on each Token in Hapi.g4
 */

class HapiErrorListener(): BaseErrorListener() {
  companion object {
    @JvmStatic
    @Throws
    fun runtimeError(token:TerminalNode , message: String) {
      val charPosition = token.getSymbol().getCharPositionInLine()
      val line = token.getSymbol().getLine()
      return this.throwError(line, charPosition, message)
    }

    fun throwError(line: Int, charPositionInLine: Int, message: String): Nothing {
      val errorMessage = "line " + line + ":" + charPositionInLine + " " + message
      System.out.println(errorMessage)
      throw ParseCancellationException(errorMessage)
    }
  }

  override fun syntaxError(
  recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int,
  charPositionInLine: Int, message: String, e: RecognitionException?){
    HapiErrorListener.throwError(line, charPositionInLine, message)
  }
}