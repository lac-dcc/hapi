package helpers

import java.io.File

import hapi.*
import utils.*

fun irFromFile(file: String, priority: List<String>) : IR =
  File(file).let {
    val root = getDirName(file)
    val source = it.readText()
    val datamap = evalDataMap(source, root)
    val ast = evalIR(source, root, datamap, priority) as IRNode
    ast.ir
  }

fun irFromString(source: String, priority: List<String>) : IR =
  run {
    val datamap = evalDataMap(source, "")
    val ast = evalIR(source, "", datamap, priority) as IRNode
    ast.ir
  }