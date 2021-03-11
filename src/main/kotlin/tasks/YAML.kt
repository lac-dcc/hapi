package tasks;

import java.io.File

import hapi.*
import utils.*

class YAMLGenerator {

  fun generate(ir: IR, datamap: DataMap, filename: String) {
    DataMapOrderChecker(datamap) // Check that datamap has right keys
      
    val file = File(filename);

    file.bufferedWriter().use { out ->
      val resources = datamap["Resources"]!!;
      out.write("data: ${resources.atoms(resources.TOP).unwrap()}\n");

      out.write("rules:\n");

      (ir as NonTerminal).nonTerminal.forEach { (actor, actions) ->
        out.write("  - identities:\n");
        out.write("      users: ${actor}\n");
        (actions as NonTerminal).nonTerminal.forEach { (action, resources) ->

          out.write("      ${action}:\n");
          out.write("        data: ${resources as Terminal}\n");
        }
      }
    }
  }
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

    val outputFile = changeExtension(file, "yaml")

    val yamlGenerator = YAMLGenerator();
    yamlGenerator.generate(ast.ir, datamap, outputFile);
  }
}
