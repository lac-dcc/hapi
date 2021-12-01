package bench

import java.io.File
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets.UTF_8
import java.util.zip.GZIPOutputStream

import kotlin.system.measureTimeMillis
import kotlin.system.exitProcess
import kotlinx.cli.*

import mock.*
import hapi.*
import tasks.usage
import tasks.YAMLGenerator
import utils.*

/* The program arguments are the following
*
* numPosets the number of posets in the product poset
* numElms (array) the number of elements in each poset
* posetDepth (array) the depth of each poset
* policyLength the number of paired allow/deny's in the policy
* policyDepth the maximum depth (number of nested statements) of
*              the policy
*/
//  gradle -q benchmarks --numPosets=3 --numElms=6 --posetDepth=2 --policyLength=5 --policyDepth=2
data class Arguments(val parserName: String, val args: Array<String>){
  private val parser = ArgParser(parserName)
  val numPosets by parser.option(ArgType.Int, "numPosets",
    description = "Max number of posets in the product poset (its dimension).").required()

  val numElms by parser.option(ArgType.Int, "numElms",
    description = "Max number of elements in each poset.").required()

  val posetDepth by parser.option(ArgType.Int, "posetDepth",
    description = "Max depth of each poset.").required()

  val policyLength by parser.option(ArgType.Int, "policyLength",
    description = "Max paired length of the policy.").required()

  val policyDepth by parser.option(ArgType.Int, "policyDepth",
    description = "Max depth of the policy.").required()
  
  init {
    parser.parse(args)
  }
}

fun gzip(content: String): ByteArray {
  val bos = ByteArrayOutputStream()
  GZIPOutputStream(bos).bufferedWriter(UTF_8).use { it.write(content) }
  return bos.toByteArray()
}

fun main(args: Array<String>) {
  lateinit var argData: Arguments
  try {
      argData = Arguments("Hapi benchmarks tool", args) 
  } catch(e:IllegalStateException){
      println(e.message)
      exitProcess(1)
  }


  println("numPosets;numElms;posetDepth;policyLength;policyDepth;"+
          "yamlBytesQtt;hapiBytesQtt;onlyPolicyBytesQtt;onlyPosetsBytesQtt")
  val yamlGenerator = YAMLGenerator();

  for (i in 2..argData.numElms){
    for (j in 2..argData.posetDepth){
  

      /* 1. Create a map of posets -> ProductPoset */
      val posets = mutableListOf<PosetElement>(
        PosetElement("Actors", j, i),
        PosetElement("Actions", j, i),
        PosetElement("Resources", j, i)
      )
      val productPoset = posets.associateBy({it.label}, {it})


      /* 2. Create the random policy based on given parameters */
      var yamlBytesQtt = 0
      var hapiBytesQtt = 0
      var onlyPolicyBytesQtt = 0
      val pol = Policy(IRType.DENY, productPoset)
      for(round in 1..100){
        pol.generateRandom(argData.policyLength, argData.policyDepth)
        val source = pol.toString()
        val datamap = evalDataMap(source, "")
        val ast = evalIR(source, "", datamap) as IRNode
        yamlGenerator.generate(ast.ir, datamap, "benchmark.yaml");
        val file = File("benchmark.yaml")
        val yamlSource = file.readText()

        /* 3. Measure time to parse the created policy */
        onlyPolicyBytesQtt += gzip(pol.policyToString()).count()
        hapiBytesQtt += gzip(source).count()
        yamlBytesQtt += gzip(yamlSource).count()
      }
      val onlyPosetsBytesQtt = gzip(pol.posetToString()).count()

      println("3;$i;$j;${argData.policyLength};${argData.policyDepth};"+
              "${yamlBytesQtt/100};${hapiBytesQtt/100};"+
              "${onlyPolicyBytesQtt/100};${onlyPosetsBytesQtt}")
    }
  }
}
