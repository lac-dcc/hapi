package tasks;

import java.io.File

import hapi.*
import utils.*

class YAMLGenerator {

  fun generate(ir: IR, datamap: DataMap, filename: String) {
    val file = File(filename);

    file.bufferedWriter().use { out ->
      val resources = datamap["Resources"]!!;
      out.write("data: ${resources.atoms(resources.TOP)}\n");

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

  val priority = listOf("Actors", "Actions", "Resources")
  val ast = IR.generate(args[0], priority) as IRNode
  
  val outputFile = changeExtension(args[0], "yaml")

  val yamlGenerator = YAMLGenerator();
  yamlGenerator.generate(ast.ir, ast.dm, outputFile);
}
