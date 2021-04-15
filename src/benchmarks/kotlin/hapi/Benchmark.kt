package hapi;

import java.io.File
import kotlin.system.measureTimeMillis

import hapi.*
import tasks.usage
import tasks.YAMLGenerator
import utils.*

/* The program arguments are of the format
 * <size-of-poset> <depth-of-poset> <size-of-policy> <depth-of-policy>
 */
fun main(args: Array<String>) {
    if (args.size < 1) {
        return usage()
    }

    println("bladjfa")
    args.forEach{println(it)}
    println(args.size)

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
