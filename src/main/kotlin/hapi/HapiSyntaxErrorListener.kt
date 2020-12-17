package hapi

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.misc.ParseCancellationException

/* 
create a decision map to manipulate errors that occurs on each Token in Hapi.g4
 */

class HapiSyntaxErrorListener(): BaseErrorListener() {
  override fun syntaxError(
  recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int,
  charPositionInLine: Int, message: String, e: RecognitionException?){

    val stack = (recognizer as Parser).getRuleInvocationStack()
    // Collections.reverse(stack)
    stack.reverse()
    // System.out.println("rule stack: "+stack)
    // System.out.println("line "+line+":"+charPositionInLine+" "+message)
    // System.out.println("line "+line+":"+charPositionInLine+" at "+
      // offendingSymbol+": "+message)

    val ruleContext = (recognizer as Parser).getRuleContext()
    for(i in 0..ruleContext.getChildCount()){
      System.out.println(ruleContext.getChild(i).getText())
    }
      
    // System.out.println(ruleContext.getText())
    // System.out.println(ruleContext.toString(recognizer))
    
    // System.out.println((recognizer as Parser).getExpectedTokensWithinCurrentRule().toString(recognizer.getVocabulary()))
    // System.out.println((recognizer as Parser).getExpectedTokensWithinCurrentRule().toString(recognizer.getVocabulary()))
    // System.out.println("line " + line + ":" + charPositionInLine + " " + message)
    throw ParseCancellationException("line " + line + ":" + charPositionInLine + " " + message)
  }
}