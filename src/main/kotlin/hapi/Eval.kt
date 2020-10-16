package hapi

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree

import java.io.FileInputStream
import java.io.InputStream

import HapiLexer
import HapiParser

fun parse(file: String): HapiParser.ProgramContext = 
  FileInputStream(file).let {
    val input = CharStreams.fromStream(it)
    val lexer = HapiLexer(input)
    val tokens = CommonTokenStream(lexer)
    val parser = HapiParser(tokens)
    parser.program()
  }

fun genDataMap(file: String): DataMap = 
  parse(file).let {
    val eval = DataVisitor(file)
    eval.visit(it)
  }
  
fun genIR(file: String, datamap: DataMap, priority: List<String>): ASTNode = 
  parse(file).let {
    val eval = IRVisitor(file, datamap, priority)
    eval.visit(it)
  }