package tasks

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree

import java.io.FileInputStream
import java.io.InputStream

import HapiLexer
import HapiParser

import hapi.*

fun usage() {
  println("usage: ./program <module>");
}

fun main(args: Array<String>) {

  if (args.size < 1){
    return usage();
  }

  val file = args[0]
  val priority = listOf("Actors", "Actions", "Resources")
  val datamap = genDataMap(file)
  val ast = genIR(file, datamap, priority) as IRNode
  
  println(ast.ir);
}
