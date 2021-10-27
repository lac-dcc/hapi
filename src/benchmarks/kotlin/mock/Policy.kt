package mock

import kotlin.random.Random
import hapi.*

class Policy(val type: IRType,
  val posets: Map<String, PosetElement>) {

  // var startClause = Clause(type, mutableListOf<Pair<String, String>>())
  lateinit var startClause: Clause
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
    if(this.posetsInArrays.isEmpty()){
      for ((posetName, posetElm) in this.posets){
        this.posetsInArrays[posetName] = posetElm.toArray()
      }
    }
    this.startClause = Clause(this.type, mutableListOf<Pair<String, String>>())
    this.inDepthGeneration(this.startClause, policyLength, policyDepth)
  }

  override public fun toString(): String {
    return posetToString() + "\n" + policyToString()
  }

  public fun posetToString(): String {
    var computedString = ""
    for ( (posetName, posetElm) in this.posets){
      computedString += "data " + posetName + " = " + posetElm.posetStructure() + "\n"
    }    
    return computedString
  }

  public fun policyToString(): String {
    return "main = \n"+ this.startClause.toString() + ";"
  }
  
}