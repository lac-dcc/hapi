package utils

import hapi.*

class DataMapOrderError(message: String) : Exception(message)

/* Checks that the datamap provided has exactly three keys, in the
 *   following order: ("Actors", "Actions", "Resources").
 */
class DataMapOrderChecker(datamap: DataMap) {
  init {
    val dmKeys = datamap.keys.toList()
    val expectedDmKeys = listOf("Actors", "Actions", "Resources")

    if (dmKeys != expectedDmKeys) {
      throw DataMapOrderError("Expected data statements for ${expectedDmKeys.toString()}" +
        ", got ${dmKeys.toString()}")
    }
  }
}
