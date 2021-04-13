package hapi;

import java.io.File
import kotlin.system.measureTimeMillis

import hapi.*
import tasks.usage
import utils.*

/* The program arguments are of the format
 * <size-of-poset> <depth-of-poset> <size-of-policy> <depth-of-policy>
 */
fun main(args: Array<String>) {
    if (args.size < 1) {
        return usage()
    }

    // Create the random policy based on given parameters
    ////////////////////////////////////////////////////////////

    // Measure time to parse the created policy
    val elapsed = measureTimeMillis {
        // Create the IR (parsing)
    }
    ////////////////////////////////////////////////////////////

    // Write time in the output file
    println("Elapsed time: $elapsed")
}
