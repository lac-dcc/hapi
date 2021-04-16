package hapi;

import java.io.File
import kotlin.system.measureTimeMillis

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
fun main(args: Array<String>) {
    if (args.size < 1) {
        return usage()
    }

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
