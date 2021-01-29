package hapi.error

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.misc.ParseCancellationException

/* 
create a decision map to manipulate errors that occurs on each Token in Hapi.g4
 */

class HapiErrorListener(): BaseErrorListener() {
  override fun syntaxError(
  recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int,
  charPositionInLine: Int, message: String, e: RecognitionException?){
    val errorMessage = "line " + line + ":" + charPositionInLine + " " + message
    System.out.println(errorMessage)
    throw ParseCancellationException(errorMessage)
  }
}