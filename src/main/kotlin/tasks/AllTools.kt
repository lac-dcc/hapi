package tasks

import hapi.*
import utils.*

import java.io.File

/**
 * In a single function block, runs all serialization tools currently supported.
 * This is called by the gradle task "all-tools" and "build-all-tools", and as
 * of now, is used mainly by Hapi Visualizer as a facilitator.
 *
 * @param args the Hapi policy file name. The output serialized files shall have
 *             the same root name (everything but the extension), including the
 *             specified directory.
 */
fun main(args: Array<String>) {
  if (args.size < 1) {
    return usage();
  }
  val filepath = args[0]
  val sourceFile = File(filepath)
  val root = getDirName(filepath)
  val sourceText = sourceFile.readText()
  
  val datamap = evalDataMap(sourceText, root)
  DataMapOrderChecker(datamap) // Check that datamap has right keys  
  val irNode = evalIR(sourceText, root, datamap) as IRNode

  val actorsDataMap = datamap.getValue("Actors")
  val actionsDataMap = datamap.getValue("Actions")
  val resourcesDataMap = datamap.getValue("Resources")

  // matrix creation
  val matrix = MatrixPrinter(actorsDataMap.elements(), actionsDataMap.elements(),
                             resourcesDataMap.elements())
  matrix.populateMatrix(irNode.ir)

  // generates HTML and JSON
  var matrixOutputFile = changeExtension(filepath, "html")
  matrix.generateHtmlFile(matrixOutputFile)
  matrixOutputFile = changeExtension(filepath, "json")
  matrix.generateJsonFile(matrixOutputFile)

  // generates .dot files
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

  // generates YAML
  val yamlOutputFile = changeExtension(filepath, "yaml")
  val yamlGenerator = YAMLGenerator();
  yamlGenerator.generate(irNode.ir, datamap, yamlOutputFile);

  // print datamap
  println(datamap.toString().replace('=', ':'))
}
