package legalease

import utils.*

import LegaleaseBaseVisitor
import LegaleaseParser

const val entryPoint = "main"

typealias DataMap = HashMap<String, Lattice>
typealias Environment = HashMap<String, IR>

sealed class ASTNode {}
data class EnvNode (val env: Environment, val dm: DataMap): ASTNode() {}
data class IRNode (val ir: IR, val dm: DataMap): ASTNode() {}

class Visitor(
  val file: String,
  val priority: List<String>    // @TODO: think of a way to remove this
  ) : LegaleaseBaseVisitor<ASTNode>() {

  val env: Environment = hashMapOf()
  val datamap: DataMap = hashMapOf()
  var namespace = "Main"

  fun attrsFrom(ctxs: List<LegaleaseParser.AttributeContext>): Map<String, Set<String>> {
    return this.priority.associateWith({
      attr -> 
        val ctx = ctxs.firstOrNull{ it.ID().toString() == attr }
        if (ctx != null){
          if (ctx.value().isEmpty()) { // attribute has no value -> top of the lattice
            datamap[attr]!!.atoms(datamap[attr]!!.TOP)
          }else{
            ctx.value().flatMap{ datamap[attr]!!.atoms(it.ID().toString()) }.toSet()
          }
        }else{ // clausule has no such attribute -> empty set
          setOf<String>()
        }
    })
  }

  override fun visitExecutable(ctx: LegaleaseParser.ExecutableContext): ASTNode {
    for (stmt in ctx.importStmt()) {
      val envNode = (visit(stmt) as EnvNode)

      this.env.putAll(envNode.env)
      this.datamap.putAll(envNode.dm)
    }

    for (stmt in ctx.dataStmt()) {
      val dataStmt = (visit(stmt) as EnvNode).dm
      this.datamap.putAll(dataStmt)
    }

    for (stmt in ctx.letStmt()) {
      val env = (visit(stmt) as EnvNode).env
      this.env.putAll(env)
    }
    
    val main = this.env.get(this.namespace + "::" + entryPoint)
    if (main != null)
      return IRNode(main, this.datamap)
    else
      throw Exception("no entry point 'main' provided")
  }

  override fun visitLibrary(ctx: LegaleaseParser.LibraryContext ): ASTNode {
    this.namespace = ctx.exportStmt().ID().toString()

    for (stmt in ctx.importStmt()) {
      val envNode = (visit(stmt) as EnvNode)

      this.env.putAll(envNode.env)
      this.datamap.putAll(envNode.dm)
    }

    for (stmt in ctx.exportStmt().dataStmt()) {
      val datamap = (visit(stmt) as EnvNode).dm
      this.datamap.putAll(datamap)
    }

    for (stmt in ctx.exportStmt().letStmt()) {
      val env = (visit(stmt) as EnvNode).env
      this.env.putAll(env)
    }

    return EnvNode(this.env, this.datamap)
  }

  override fun visitImportStmt(ctx: LegaleaseParser.ImportStmtContext): ASTNode {
    val module = ctx.ID().toString()

    val file = changeFileName(this.file, module)

    val ast = IR.generate(file, this.priority)

    return when (ast) {
      is EnvNode -> ast
      else -> throw Exception("can't import an executable file")
    }
  }

  override fun visitLetStmt(ctx: LegaleaseParser.LetStmtContext): ASTNode {
    val id = ctx.ID().toString()
    val key = this.namespace + "::" + id
    val ir = (visit(ctx.policyExpr()) as IRNode).ir
    val env: Environment = hashMapOf(key to ir)
    return EnvNode(env, hashMapOf())
  }

  override fun visitDataStmt(ctx: LegaleaseParser.DataStmtContext): ASTNode {
    val lattice = Lattice();
    val id = ctx.ID().toString()

    for (elem in ctx.elem()){
      val parent = elem.ID().toString()
      val children = elem.value()

      if (!lattice.contains(parent))
        lattice.append(lattice.TOP, parent)

      for (child in children)
        lattice.append(parent, child.ID().toString())
    }

    val datamap = hashMapOf(id to lattice)
    return EnvNode(Environment(), datamap)
  }

  fun getIRfromLiteralExpr(literalExpr: LegaleaseParser.LiteralExprContext): IR {

    var literal = literalExpr.ID().map {e -> e.toString()}.toMutableList()
    if (literal.size == 1)
      literal.add(0, this.namespace)
    
    val name = literal.joinToString(separator="::")

    val ir = this.env.get(name)

    if (ir != null)
      return ir
    else
      throw Exception("undefined name: ${name}")
  }

  override fun visitAttributeExpr(ctx: LegaleaseParser.AttributeExprContext): ASTNode {
    val literal = ctx.literalExpr()
    val ir =  if (literal != null)
                getIRfromLiteralExpr(literal)
              else
                IR.from(attrsFrom(ctx.attribute()), this.priority)

    return IRNode(ir, this.datamap)
  }

  override fun visitDenyExpr(ctx: LegaleaseParser.DenyExprContext): ASTNode {

    if (ctx.attributeExpr() == null) {
      val ir =  ctx.exceptAllow().allowExpr()
                .map{(visit(it) as IRNode).ir}
                .reduce({ acc, ir -> acc.plus(ir).unwrap() })
      return IRNode(ir, this.datamap)
    }

    val denyAttr = visit(ctx.attributeExpr())
    val denyAttrIR = (denyAttr as IRNode).ir
    val exceptCtx = ctx.exceptAllow()

    if (exceptCtx != null){
      val ir = exceptCtx.allowExpr()
              .map{(visit(it) as IRNode).ir}
              .fold(denyAttrIR, { acc, ir -> acc.minus(ir).unwrap() })
      return IRNode(ir, this.datamap)
    }

    return denyAttr
  }

  override fun visitAllowExpr(ctx: LegaleaseParser.AllowExprContext): ASTNode {

    if (ctx.attributeExpr() == null) {
      val top = this.datamap.mapValues { (_, lattice) -> lattice.atoms(lattice.TOP) }
      val topIR = IR.from(top, this.priority)

      val ir = ctx.exceptDeny().denyExpr()
              .map{(visit(it) as IRNode).ir}
              .fold(topIR, { acc, ir -> acc.minus(ir).unwrap() })

      return IRNode(ir, this.datamap)
    }

    val allowAttr = visit(ctx.attributeExpr())
    val allowAttrIR = (allowAttr as IRNode).ir
    val exceptCtx = ctx.exceptDeny()

    if (exceptCtx != null){
      val ir = exceptCtx.denyExpr()
              .map{(visit(it) as IRNode).ir}
              .fold(allowAttrIR, { acc, ir -> acc.minus(ir).unwrap() })
      return IRNode(ir, this.datamap)
    }

    return allowAttr
  }

  override fun visitExceptAllow(ctx: LegaleaseParser.ExceptAllowContext): ASTNode { 
    val ir = ctx.allowExpr()
            .map{(visit(it) as IRNode).ir}
            .reduce({ acc, ir -> acc.plus(ir).unwrap() })
    return IRNode(ir, this.datamap)
  }

  override fun visitExceptDeny(ctx: LegaleaseParser.ExceptDenyContext): ASTNode {
    val ir = ctx.denyExpr()
            .map{(visit(it) as IRNode).ir}
            .reduce({ acc, ir -> acc.plus(ir).unwrap() })
    return IRNode(ir, this.datamap)
  }
}