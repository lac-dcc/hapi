package hapi

import java.io.File
import java.util.Stack

class Lattice(){

  val TOP = "⊤"
  val BOTTOM = "⊥"
  private var adj: MutableMap<String, MutableSet<String>> = mutableMapOf(this.TOP to mutableSetOf())

  fun append(parent: String, elem: String) {
    val parentElem = this.adj[parent]
    if (parentElem != null){
      
      if (!this.adj.containsKey(elem))
        this.adj.put(elem, mutableSetOf())

      parentElem.add(elem)
    }
  }

  fun contains(elem: String): Boolean {
    var found = false
    this.dfs(TOP, {node, _ -> found = found || elem == node})
    return found
  }

  fun dfs(label: String, closure: (String, MutableSet<String>?) -> Unit) {
    val stack = Stack<String>()
    val visited = mutableSetOf<String>()

    stack.push(label)

    while(!stack.empty()){
      val elem = stack.pop()
      closure(elem, this.adj[elem]!!)
      visited.add(elem)

      this.adj[elem]!!.forEach({
        if(!visited.contains(it))
          stack.add(it)
      })
    }
  }

  fun atoms(label: String): MutableSet<String> {
    val atoms: MutableSet<String> = mutableSetOf()
    dfs(label, {
      node, neigh ->
      if(neigh.isNullOrEmpty()) 
        atoms.add(node)
      })
    return atoms
  }

  override fun toString(): String {
        return this.adj.toString()
    }
}