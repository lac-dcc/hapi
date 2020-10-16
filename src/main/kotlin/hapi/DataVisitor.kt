package hapi

import utils.*

import HapiBaseVisitor
import HapiParser

typealias DataMap = Map<String, Lattice>

class DataVisitor(val file: String) : HapiBaseVisitor<DataMap>() {

  override fun visitExecutable(ctx: HapiParser.ExecutableContext): DataMap {
    return ctx.stmt().fold(mapOf(), {
      datamap, stmt -> visit(stmt).let { if(it != null) datamap + it else datamap}
    })
  }

  override fun visitLibrary(ctx: HapiParser.LibraryContext ): DataMap {
    return ctx.stmt().fold(mapOf(), {
      datamap, stmt -> visit(stmt).let { if (it != null) datamap + it else datamap}
    })
  }

  override fun visitImportStmt(ctx: HapiParser.ImportStmtContext): DataMap {
    val module = ctx.ID().toString()
    val file = changeFileName(this.file, module)

    return genDataMap(file)
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