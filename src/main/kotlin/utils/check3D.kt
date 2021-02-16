package utils

import hapi.*

class check3DException(message: String) : Exception(message)

/* Checks that the datamap provided has exactly three keys, in the
 *   following order: ("Actors", "Actions", "Resources").
 */
class check3D(datamap: DataMap) {
  init {
    val dmKeys = datamap.keys.toList()
    val expectedDmKeys = listOf("Actors", "Actions", "Resources")

    if (dmKeys != expectedDmKeys) {
      throw check3DException("Expected data statements for ${expectedDmKeys.toString()}" +
        ", got ${dmKeys.toString()}")
    }
  }
}
