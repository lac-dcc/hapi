package tasks

import hapi.*
import utils.*

import java.io.File

fun main(args: Array<String>) {
  if (args.size < 1) {
    return usage();
  }
  val filepath = args[0]
  val sourceFile = File(filepath)
  val root = getDirName(filepath)
  val sourceText = sourceFile.readText()
  val datamap = evalDataMap(sourceText, root)
  val irNode = evalIR(sourceText, root, datamap) as IRNode

  val actionsDataMap = datamap.getValue("Actions")
  val actorsDataMap = datamap.getValue("Actors")
  val resourcesDataMap = datamap.getValue("Resources")

  // generate matrix
  val matrixOutputFile = changeExtension(filepath, "html")
  val matrix = MatrixPrinter(actorsDataMap.elements(), resourcesDataMap.elements())
  matrix.populateMatrix(irNode.ir)
  matrix.generateHtmlFile(matrixOutputFile)

  // generate .dot files
  val actionsDot = relativePath(filepath, "actions.dot")
  var file = File(actionsDot);
  file.bufferedWriter().use { out ->
    out.write(actionsDataMap.dot_graph());
  }

  val actorsDot = relativePath(filepath, "actors.dot")
  file = File(actorsDot);
  file.bufferedWriter().use { out ->
    out.write(actorsDataMap.dot_graph());
  }

  val resourcesDot = relativePath(filepath, "resources.dot")
  file = File(resourcesDot);
  file.bufferedWriter().use { out ->
    out.write(resourcesDataMap.dot_graph());
  }

  // generate Yaml
  val yamlOutputFile = changeExtension(filepath, "yaml")
  val yamlGenerator = YAMLGenerator();
  yamlGenerator.generate(irNode.ir, datamap, yamlOutputFile);

  // print datamap
  println(datamap.toString().replace('=', ':'))
}
