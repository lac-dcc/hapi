package helpers

import java.io.File

import hapi.*
import utils.*

fun irFromFile(file: String) : IR =
  File(file).let {
    val root = getDirName(file)
    val source = it.readText()
    val datamap = evalDataMap(source, root)
    val ast = evalIR(source, root, datamap) as IRNode
    ast.ir
  }

fun irFromString(source: String) : IR =
  run {
    val datamap = evalDataMap(source, "")
    val ast = evalIR(source, "", datamap) as IRNode
    ast.ir
  }