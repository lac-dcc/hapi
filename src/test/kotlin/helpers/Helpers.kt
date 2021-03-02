package helpers

import java.io.File

import hapi.*
import tasks.YAMLGenerator
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

/* Generates YAML translation of Hapi program from "file"
 * Returns the name of the resulting translation
 */
fun yamlFromFile(file: String) : String =
  File(file).let {
    val root = getDirName(file)
    val source = it.readText()
    val datamap = evalDataMap(source, root)
    val ast = evalIR(source, root, datamap) as IRNode

    val outputFile = changeExtension(file, "yaml")

    val yamlGenerator = YAMLGenerator()
    yamlGenerator.generate(ast.ir, datamap, outputFile)

    outputFile
  }
