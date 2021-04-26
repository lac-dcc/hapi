package mock

import kotlin.random.Random
import hapi.*

class Policy(val type: IRType,
  val posets: Map<String, PosetElement>) {

  val startClause = Clause(type, mutableListOf<Pair<String, String>>())
  private val posetsInArrays = mutableMapOf<String, Array<PosetElement>>()

  
  private fun randomProductPoset(): List<Pair<String, String>> {
    val result = mutableListOf<Pair<String, String>>()
    for ( (posetName, posetArr) in this.posetsInArrays){
      result.add(posetName to (posetArr[Random.nextInt(0, posetArr.count()-1)].toString()))
    }
    return (result as List<Pair<String, String>>)
  }

  private fun inDepthGeneration(parentClause: Clause,
    policyLength: Int, policyDepth: Int){

    val currentType = if (parentClause.type == IRType.ALLOW) IRType.DENY else IRType.ALLOW
    if(policyDepth > 0){
      for( i in 1..policyLength){
        val currentClause = Clause(currentType, this.randomProductPoset())
        this.inDepthGeneration(currentClause, policyLength, policyDepth - 1)
        parentClause.addException(currentClause)
      }
    }
  }
  
  public fun generateRandom(policyLength: Int, policyDepth: Int){
    for ((posetName, posetElm) in this.posets){
      posetsInArrays[posetName] = posetElm.toArray()
    }
    this.inDepthGeneration(this.startClause, policyLength, policyDepth)
  }

  override public fun toString(): String {
    var computedString = ""
    for ( (posetName, posetElm) in this.posets){
      computedString += "data " + posetName + " = " + posetElm.posetStructure() + "\n"
    }
    computedString += "\nmain = \n"+ this.startClause.toString() + ";"
    
    return computedString
  }
  
}