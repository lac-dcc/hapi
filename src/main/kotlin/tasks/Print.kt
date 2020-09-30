package tasks;

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree

import java.io.FileInputStream
import java.io.InputStream

import LegaleaseLexer
import LegaleaseParser

import legalease.*

fun usage() {
  println("usage: ./program <module>");
}

fun main(args: Array<String>) {

  if (args.size < 1){
    return usage();
  }

  val priority = listOf("Actors", "Actions", "Resources")
  val ast = IR.generate(args[0], priority) as IRNode
  
  println(ast.ir);
}
