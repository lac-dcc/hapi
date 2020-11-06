package tasks

import hapi.*
import utils.*

import java.io.File

fun main(args: Array<String>){
  if (args.size < 1){
    return usage();
  }
  val inputFile = args[0]

  val priority = listOf("Actors", "Actions", "Resources")
  val irNode = (IR.generate(inputFile, priority) as IRNode)


  val actionsDataMap = irNode.dm.getValue("Actions")
  val actorsDataMap = irNode.dm.getValue("Actors")
  val resourcesDataMap = irNode.dm.getValue("Resources")

  // generate matrix
  val matrixOutputFile = changeExtension(inputFile, "html")
  val matrix = MatrixPrinter(actorsDataMap.elements(), resourcesDataMap.elements())
  matrix.populate_matrix(irNode.ir)
  matrix.generate_html_file(matrixOutputFile)


  // generate .dot files
  val actionsDot = relativePath(inputFile, "actions.dot")
  // println(actionsDataMap.dot_graph())
  var file = File(actionsDot);
  file.bufferedWriter().use { out ->
    out.write(actionsDataMap.dot_graph());
  }

  val actorsDot = relativePath(inputFile, "actors.dot")
  file = File(actorsDot);
  file.bufferedWriter().use { out ->
    out.write(actorsDataMap.dot_graph());
  }

  val resourcesDot = relativePath(inputFile, "resources.dot")
  // println(resourcesDataMap.dot_graph())
  file = File(resourcesDot);
  file.bufferedWriter().use { out ->
    out.write(resourcesDataMap.dot_graph());
  }

  // generate Yaml
  val yamlOutputFile = changeExtension(inputFile, "yaml")
  val yamlGenerator = YAMLGenerator();
  yamlGenerator.generate(irNode.ir, irNode.dm, yamlOutputFile);

  // print datamap
  println(irNode.dm.toString().replace('=', ':'))
}