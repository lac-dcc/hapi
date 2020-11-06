package tasks

import java.io.File

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree

import java.io.FileInputStream
import java.io.InputStream

import HapiLexer
import HapiParser

import hapi.*
import utils.*

fun usage() {
  println("usage: ./program <module>");
}

fun main(args: Array<String>) {

  if (args.size < 1){
    return usage();
  }

  val file = args[0]

  File(file).let {
    val root = getDirName(file)
    val source = it.readText()
    val datamap = evalDataMap(source, root)
    val ast = evalIR(source, root, datamap) as IRNode
    println(ast.ir);
  }
}
