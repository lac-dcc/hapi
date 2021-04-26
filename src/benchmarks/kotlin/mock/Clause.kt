package mock

import hapi.*

class Clause(val type: IRType,
  val productPoset: List<Pair<String, String>>) {

  private val exceptions = mutableListOf<Clause>()
  
  public fun addException(excp: Clause){
    exceptions.add(excp)
  }

  private fun productPosetString(): String {
    var computedString = ""
    if(productPoset.count() > 0){
      computedString += " {\n"
      for (posetEl in productPoset){
        computedString += posetEl.first + ": " + posetEl.second + "\n"
      }
      computedString += "} "
    } else {
      computedString = "\n"
    }
    return computedString
  }

  override public fun toString(): String {
    var computedString = ""
    if (type == IRType.ALLOW){
      computedString += "ALLOW"
    } else {
      computedString += "DENY"
    }
    computedString += productPosetString()
    
    if (exceptions.count() > 0){
      computedString += "EXCEPT {\n"
      for (exp in exceptions){
        computedString += exp.toString()
      }
      computedString += "}"
    }
    computedString += "\n"
    return computedString
  }
}