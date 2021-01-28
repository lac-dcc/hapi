package hapi

import java.io.File
import java.io.FileNotFoundException

import utils.*
import hapi.error.*

import HapiParser
import HapiBaseVisitor

typealias DataMap = Map<String, Lattice>

class DataVisitor(val root: String) : HapiBaseVisitor<DataMap>() {

  override fun visitExecutable(ctx: HapiParser.ExecutableContext): DataMap {
    return ctx.stmt().fold(mapOf(), {
      datamap, stmt -> visit(stmt).let { if(it != null) datamap + it else datamap}
    })
  }

  override fun visitLibrary(ctx: HapiParser.LibraryContext ): DataMap {
    return ctx.stmt().fold(mapOf(), {
      datamap, stmt -> visit(stmt).let { if(it != null) datamap + it else datamap}
    })
  }

  override fun visitImportStmt(ctx: HapiParser.ImportStmtContext): DataMap {
    val module = this.root + "/" + ctx.ID().toString() + ".hp"
    return File(module).let {
      if (!it.exists()){
        val message = "Module '${ctx.ID()}' does not exist"
        throw HapiRuntimeException(ctx.ID(), message)
      } 
      evalDataMap(it.readText(), this.root)
    }
  }

  override fun visitDataStmt(ctx: HapiParser.DataStmtContext): DataMap {
    val lattice = Lattice();
    val id = ctx.ID().toString()

    for (elem in ctx.dataElem()) {
      val parent = elem.ID().toString()

      if (!lattice.contains(parent))
        lattice.append(lattice.TOP, parent)

      val children = elem.value()
      
      if (children != null)
        for (child in children)
          lattice.append(parent, child.ID().toString())
    }

    return mapOf(id to lattice)
  }
}