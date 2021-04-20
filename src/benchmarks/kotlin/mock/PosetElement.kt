package mock;

import bench.*

/* posets are declared as a n-tree where for each level we
have $n$ elements per nodes
*/
class PosetElement(val label: String, val level: Int, val childrenQtt: Int){
  private val children = mutableListOf<PosetElement>()

  init {
    if(level > 0){
      var lastChidrenName : String = label+"_"
      for(i in 0..childrenQtt){
        lastChidrenName = nextLexicographString(lastChidrenName)
        children.add(PosetElement(lastChidrenName, level-1, childrenQtt))
      }
    }
  }

  override public fun toString(): String {
    var computedString = label
    if(this.children.count() > 0){
      computedString += "("
      val elem = this.children.iterator()
      while(elem.hasNext()){
        computedString += elem.next().label
        if(elem.hasNext()){
          computedString += ", "
        }
      }
      computedString += "), "
      for (elem in this.children){
        computedString += elem.toString() + " "
      }
    }
    return computedString
  }
}