package tasks

import hapi.*
import utils.*

import java.io.OutputStreamWriter
import java.io.FileOutputStream
import java.io.File
import java.io.FileWriter

import javax.xml.transform.TransformerFactory
import javax.xml.transform.OutputKeys
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.dom.DOMSource

import com.google.gson.GsonBuilder

/**
 * The class responsible for the matrix representation of a Hapi policy.
 * <p>
 *     It implements the Kotlin matrix representation, as well as routines to
 *     generate its JSON version.
 * Usage example:
 * <pre>
 *     // generate matrix
 *     val matrix = MatrixPrinter(actorsDataMap.elements(),
 *                                resourcesDataMap.elements())
 *     matrix.populateMatrix(irNode.ir)
 *     // Generate JSON
 *     matrix.generateHtmlFile("Output.html")
 * </pre>
 */
class MatrixPrinter {
  private val actors: Map<String, Int>
  private val indexedActors: Array<String>
  private val actions: Map<String, Int>
  private val indexedActions: Array<String>
  private val resources: Map<String, Int>
  private val indexedResources: Array<String>
  private val matrix: Array<Array<Map<String, Int>>>

  /**
   * Primary constructor.
   * Constructs the internal objects indexedResources and indexedActors, and
   * initializes matrix as a dummy object.
   * @param actors    names of the actors
   * @param resources names of the resources
   */
  constructor(datamap: DataMap) {
    DataMapOrderChecker(datamap) // Check that datamap has correct keys

    // Only consider the atoms
    fun getDMAtoms(latticeName: String) =
      datamap[latticeName]!!.atoms(datamap[latticeName]!!.TOP).unwrap().sorted()
    val actors = getDMAtoms("Actors")
    val actions = getDMAtoms("Actions")
    val resources = getDMAtoms("Resources")

    // associate the actors names with the matrix indexes
    var index: Int = 0
    this.actors = actors.associateWith {index++}
    indexedActors = Array<String>(actors.size) {""}
    for ((key, value) in this.actors) {
      indexedActors[value] = key
    }

    // associate the actions names with the matrix indexes
    index = 0
    this.actions = actions.associateWith {index++}
    indexedActions = Array<String>(actions.size) {""}
    for ((key, value) in this.actions) {
      indexedActions[value] = key
    }

    // associate the resources names with the matrix indexes
    index  = 0
    this.resources = resources.associateWith {index++}
    indexedResources = Array<String>(resources.size) {""}
    for ((key, value) in this.resources) {
      indexedResources[value] = key
    }

    // create matrix
    this.matrix = Array<Array<Map<String, Int>>>(actors.size) {
      Array<Map<String, Int>>(resources.size) {
        // Initially associcate each action with zero
        actions.associateWith { 0 } as MutableMap<String, Int>
      }
    }
  }

  /**
   * Fills the matrix with the ir actors, resources and their corresponding
   * action status, for each action available.
   * @param ir the policy intermediate representation
   * @see   IR
   */
  public fun populateMatrix(ir: IR) {
    //transform the IR data in a matrix
    for ((actor, actions) in (ir as NonTerminal).nonTerminal) {
      for ((action, _resources) in (actions as NonTerminal).nonTerminal) {
        (_resources as Terminal).terminal.forEach {
          resource ->
          (this.matrix[this.actors[actor] as Int][this.resources[resource] as Int] as MutableMap).put(action, 1)
        }
      }
    }
  }

  /**
   * Generates and fills the target JSON serialized file with the properly
   * formatted contents of the Hapi matrix.
   * <p>
   *     Makes use of the JSON serialization and deserialization library
   *     <a href="https://github.com/google/gson">Gson</a>
   *     to easily create the java object.
   * @param jsonFileName the target file
   */
  public fun generateJsonFile(jsonFileName: String) {
    /*
      Aims to generate a three-layer deep map, so that GsonBuilder may be able
        to build a JSON with the right names associated.
      At a conceptual level, keys are actor, resource, action, respectively.
    */
    val gsonMap: Map<String, Map<String, Map<String, Int>>> =
      mutableMapOf<String, Map<String, Map<String, Int>>>().apply {
          putAll(indexedActors.toSet().associateWith { actor ->
              mutableMapOf<String, Map<String, Int>>().apply {
                  putAll(indexedResources.toSet().associateWith { resource ->
                      matrix[actors[actor] as Int][resources[resource] as Int]
                  })
              }
          })
      }

    val gsonPretty = GsonBuilder().setPrettyPrinting().create()
    val jsonTutsArrayPretty: String = gsonPretty.toJson(gsonMap)

    val jsonOutFile = FileWriter(jsonFileName)
    jsonOutFile.write(jsonTutsArrayPretty)
    jsonOutFile.close()
  }
}

/**
 * Builds the matrix and generates JSON output file.
 * This is called by the gradle task "matrix".
 *
 * @param args the Hapi policy file name. The output serialized files shall have
 *             the same root name (everything but the extension), including the
 *             specified directory.
 */
fun main(args: Array<String>) {
  if (args.size < 1) {
    return usage()
  }

  val filepath = args[0]
  val sourceFile = File(filepath)
  val root = getDirName(filepath)
  val sourceText = sourceFile.readText()
  
  val datamap = evalDataMap(sourceText, root)
  DataMapOrderChecker(datamap) // Check that datamap has right keys
  val irNode = evalIR(sourceText, root, datamap) as IRNode

  // generate matrix
  val matrix = MatrixPrinter(datamap)
  matrix.populateMatrix(irNode.ir)

  val matrixOutputFile = changeExtension(filepath, "json")
  matrix.generateJsonFile(matrixOutputFile);
}
