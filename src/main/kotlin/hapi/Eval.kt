package hapi

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.misc.ParseCancellationException

import java.io.InputStream

import HapiLexer
import HapiParser

fun tokenize(source: String): CommonTokenStream =
  CharStreams.fromString(source).let {
    val lexer = HapiLexer(it).apply {
      removeErrorListeners()
      addErrorListener(HapiErrorListener())
    }
    CommonTokenStream(lexer)
  }

fun parse(tokens: CommonTokenStream): HapiParser.ProgramContext =
  HapiParser(tokens).apply {
    removeErrorListeners()
    addErrorListener(HapiErrorListener())
  }.program()

fun evalDataMap(source: String, root: String): DataMap = 
  parse(tokenize(source)).let {
    val eval = DataVisitor(root)
    eval.visit(it)
  }

fun evalIR(source: String, root: String, datamap: DataMap): ASTNode = 
  parse(tokenize(source)).let {
    val eval = IRVisitor(root, datamap)
    eval.visit(it)
  }