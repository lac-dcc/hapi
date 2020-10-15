package tasks;

import hapi.*
import utils.*

import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStreamWriter
import java.io.FileOutputStream

import kotlinx.html.*
import kotlinx.html.dom.*

import javax.xml.transform.TransformerFactory
import javax.xml.transform.OutputKeys
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.dom.DOMSource

class MatrixPrinter {
  private val resources: Map<String, Int>
  private val indexed_resources: Array<String>
  private val actors: Map<String, Int>
  private val indexed_actors: Array<String>
  private val matrix: Array<Array<Map<String, Int>>>

  constructor(actors: Set<String>, resources: Set<String>){
    
    // associate the resources names with the matrix indexes
    var index: Int = 0
    this.resources = resources.associateWith({index++});
    indexed_resources = Array<String>(resources.size){""}
    for((key, value) in this.resources){
      indexed_resources[value] = key
    }
    
    // associate the actors names with the matrix indexes
    index = 0
    this.actors = actors.associateWith({index++});
    indexed_actors = Array<String>(actors.size){""}
    for((key, value) in this.actors){
      indexed_actors[value] = key
    }
    
    // create matrix
    this.matrix = Array<Array<Map<String, Int>>>(actors.size){
      Array<Map<String, Int>>(resources.size){
        mutableMapOf("Reads" to 0, "Updates" to 0, "Deletes" to 0)
      }
    }
  }

  public fun populate_matrix(ir: IR){
    //transform the IR data in a matrix
    println(ir)
    for((actor, actions) in (ir as NonTerminal).nonTerminal ){
      for ((action, _resources) in (actions as NonTerminal).nonTerminal){
        (_resources as Terminal).terminal.forEach{resource ->
          (this.matrix[this.actors[actor] as Int][this.resources[resource] as Int] as MutableMap).put(action, 1)
        }
      }
    }
  }

  public fun generate_html_file(htmFileName: String){
    val document = DocumentBuilderFactory.newInstance().
      newDocumentBuilder().newDocument()
    
    //kotlinx.html notation to generate a html page
    val html = document.create.html {
      head {
        title("Lattice Matrix")
        style {
          unsafe {
            raw(
              """
              table, th, td {
                border: 1px solid black;
                border-collapse: collapse;
              }
              th, td {
                padding: 8px 4px;
                text-align: left;    
              }
              p {
                margin: 4px 0;
              }
              """
            )
          }
        }
      }
      body {
        table{
          tr{
            th{+" "}
            for (resource_name in indexed_resources){
              th{+resource_name}
            }
          }
          for (i in 0..(matrix.size)-1){
            tr {
              th {+indexed_actors[i]}
              for (j in 0..(matrix[i].size)-1){
                val cell = matrix[i][j]
                td {
                  p{+"Reads: ${cell["Reads"]}"}
                  p{+"Updates: ${cell["Updates"]}"}
                  p{+"Deletes: ${cell["Deletes"]}"}
                }
              }
            }
          }
        }
      }
    }

    with(TransformerFactory.newInstance().newTransformer()){
      setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
      setOutputProperty(OutputKeys.METHOD, "xml")
      setOutputProperty(OutputKeys.INDENT, "yes")
      setOutputProperty(OutputKeys.ENCODING, "UTF-8")
      setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
      transform(DOMSource(html),
        StreamResult(OutputStreamWriter(FileOutputStream(htmFileName), "UTF-8")))
    }
  }
}

fun main(args: Array<String>){
  if (args.size < 1){
    return usage();
  }

  val priority = listOf("Actors", "Actions", "Resources")
  val irNode = (IR.generate(args[0], priority) as IRNode)

  val outputFile = changeExtension(args[0], "html")

  val actors = irNode.dm.getValue("Actors").elements()
  val resources = irNode.dm.getValue("Resources").elements()

  val matrix = MatrixPrinter(actors, resources);

  matrix.populate_matrix(irNode.ir)
  matrix.generate_html_file(outputFile)
}