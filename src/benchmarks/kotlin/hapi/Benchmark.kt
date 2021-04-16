package hapi;

import java.io.File
import kotlin.system.measureTimeMillis
import kotlin.system.exitProcess
import kotlinx.cli.*


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

fun main(args: Array<String>) {
    lateinit var argData: Arguments
    try {
        argData = Arguments("Hapi benchmarks tool", args) 
    } catch(e:IllegalStateException){
        println(e.message)
        exitProcess(1)
    }
    println(argData.numElms)
    

    println("Number of arguments: " + args.size.toString())
    args.forEach{println(it)}

    // Create the random policy based on given parameters
    // BEGIN TEMPORARY (dummy code)
    // val fileName = args[0]
    // val root = getDirName(fileName)
    // val source = File(fileName).readText()
    // END TEMPORARY (dummy code)
    ////////////////////////////////////////////////////////////

    // Measure time to parse the created policy
    // val elapsed = measureTimeMillis {
        // Create the IR (parsing)
        // evalIR(source, root, evalDataMap(source, root))
    // }
    ////////////////////////////////////////////////////////////

    // Write time in the output file
    // println("Elapsed time: $elapsed")
}
