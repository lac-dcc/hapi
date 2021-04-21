package mock

import bench.*

/* posets are declared as a n-tree where for each level we
have $n$ elements per nodes
*/
class PosetElement(val label: String, val level: Int, val childrenQtt: Int){
  val children = mutableListOf<PosetElement>()

  init {
    if(level > 1){
      var lastChildName : String = label+"_"
      for(i in 1..childrenQtt){
        lastChildName = nextLexicographString(lastChildName)
        children.add(PosetElement(lastChildName, level-1, childrenQtt))
      }
    }
  }

  override public fun toString(): String {
    val bfsFifo = mutableListOf<PosetElement>(this)
    var computedString = ""

    while(bfsFifo.count() > 0){
      val elem = bfsFifo.removeAt(0)
      computedString += elem.label
      
      if(elem.children.count() > 0){
        computedString += "("
        val childIt = elem.children.iterator()
        while(childIt.hasNext()){
          val child = childIt.next()
          computedString += child.label
          bfsFifo.add(child)

          if(childIt.hasNext()){
            computedString += ", "
          }
        computedString += ")"
      }
      if(bfsFifo.count() > 0){
        computedString += ", "
      } else{
        computedString += ";"
      }
    }
    return computedString
  }
}