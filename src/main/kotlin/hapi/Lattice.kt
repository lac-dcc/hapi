package hapi

import java.util.Stack

import utils.*

class Lattice() {
  val TOP = "⊤"
  val BOTTOM = "⊥"
  var adj: MutableMap<String, MutableSet<String>> = mutableMapOf(this.TOP to mutableSetOf())

  override fun toString(): String {
    return this.adj.toString()
  }
}

fun Lattice.append(parent: String, elem: String) =
  this.adj[parent].let{
    if (it != null){
      
      if (!this.adj.containsKey(elem))
        this.adj.put(elem, mutableSetOf())

      it.add(elem)
    }
  }

fun Lattice.contains(elem: String): Boolean =
  run {
    var found = false
    this.dfs(TOP, {node, _ -> found = found || elem == node})
    found
  }

fun Lattice.dfs(label: String, closure: (String, MutableSet<String>?) -> Unit) =
  run {
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

fun Lattice.atoms(label: String): Result<MutableSet<String>, String> =
  run {
    if (!this.adj.containsKey(label))
      return Err("undefined value ${label}")

    val atoms: MutableSet<String> = mutableSetOf()
    dfs(label, {
      node, neigh ->
      if(neigh.isNullOrEmpty()) 
        atoms.add(node)
      })

    Ok(atoms)
  }

  fun Lattice.elements(): Set<String>{
    return this.adj.keys
  }

  fun Lattice.dot_graph(): String{
    var result: String = ""
    
    result += "graph {\n"

    this.adj.forEach {
      val parent = it.key;
      it.value.forEach {
        result += "\t${parent} -- ${it};\n"
      }
    }
    result += "}\n"

    return result
  }