package hapi

import utils.*

import HapiBaseVisitor
import HapiParser

const val entryPoint = "main"

typealias DataMap = HashMap<String, Lattice>
typealias Environment = HashMap<String, IR>

sealed class ASTNode {}
data class EnvNode (val env: Environment, val dm: DataMap): ASTNode() {}
data class IRNode (val ir: IR, val dm: DataMap): ASTNode() {}

class Visitor(
  val file: String,
  val priority: List<String>    // @TODO: think of a way to remove this
  ) : HapiBaseVisitor<ASTNode>() {

  val env: Environment = hashMapOf()
  val datamap: DataMap = hashMapOf()
  var namespace = "Main"

  fun attrsFrom(ctxs: List<HapiParser.AttributeContext>): Map<String, Set<String>> {
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

  override fun visitExecutable(ctx: HapiParser.ExecutableContext): ASTNode {
    for (stmt in ctx.stmt()) {
      val envNode = visit(stmt) as EnvNode

      this.env.putAll(envNode.env)
      this.datamap.putAll(envNode.dm)
    }

    val main = this.env.get(this.namespace + "::" + entryPoint)
    if (main != null)
      return IRNode(main, this.datamap)
    else
      throw Exception("no entry point 'main' provided")
  }

  override fun visitLibrary(ctx: HapiParser.LibraryContext ): ASTNode {
    this.namespace = ctx.exportStmt().ID().toString()

    for (stmt in ctx.stmt()) {
      val envNode = visit(stmt) as EnvNode

      this.env.putAll(envNode.env)
      this.datamap.putAll(envNode.dm)
    }

    return EnvNode(this.env, this.datamap)
  }

  override fun visitImportStmt(ctx: HapiParser.ImportStmtContext): ASTNode {
    val module = ctx.ID().toString()

    val file = changeFileName(this.file, module)

    val ast = IR.generate(file, this.priority)

    return when (ast) {
      is EnvNode -> ast
      else -> throw Exception("can't import an executable file")
    }
  }

  override fun visitLetStmt(ctx: HapiParser.LetStmtContext): ASTNode {
    val id = ctx.ID().toString()
    val key = this.namespace + "::" + id
    val ir = (visit(ctx.policyExpr()) as IRNode).ir
    val env: Environment = hashMapOf(key to ir)
    return EnvNode(env, hashMapOf())
  }

  override fun visitDataStmt(ctx: HapiParser.DataStmtContext): ASTNode {
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

    val datamap = hashMapOf(id to lattice)
    return EnvNode(Environment(), datamap)
  }

  fun getIRfromLiteralExpr(literalExpr: HapiParser.LiteralExprContext): IR {

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

  override fun visitAttributeExpr(ctx: HapiParser.AttributeExprContext): ASTNode {
    val literal = ctx.literalExpr()
    val ir =  if (literal != null)
                getIRfromLiteralExpr(literal)
              else
                IR.from(attrsFrom(ctx.attribute()), this.priority)

    return IRNode(ir, this.datamap)
  }

  override fun visitDenyExceptExpr(ctx: HapiParser.DenyExceptExprContext): ASTNode {

    val denyAttr = visit(ctx.attributeExpr())
    val denyAttrIR = (denyAttr as IRNode).ir

    val ir = ctx.allowExpr()
            .map{(visit(it) as IRNode).ir}
            .fold(denyAttrIR, { acc, ir -> acc.minus(ir).unwrap() })
    return IRNode(ir, this.datamap)
  }

  override fun visitDenyAllExceptExpr(ctx: HapiParser.DenyAllExceptExprContext): ASTNode {
    val ir = ctx.allowExpr()
              .map{(visit(it) as IRNode).ir}
              .reduce({ acc, ir -> acc.plus(ir).unwrap() })
    return IRNode(ir, this.datamap)
  }

  override fun visitAllowExceptExpr(ctx: HapiParser.AllowExceptExprContext): ASTNode {

    val allowAttr = visit(ctx.attributeExpr())
    val allowAttrIR = (allowAttr as IRNode).ir

    val ir = ctx.denyExpr()
            .map{(visit(it) as IRNode).ir}
            .fold(allowAttrIR, { acc, ir -> acc.minus(ir).unwrap() })
    return IRNode(ir, this.datamap)
  }

  override fun visitAllowAllExceptExpr(ctx: HapiParser.AllowAllExceptExprContext): ASTNode {

    val top = this.datamap.mapValues { (_, lattice) -> lattice.atoms(lattice.TOP) }
    val topIR = IR.from(top, this.priority)

    val ir = ctx.denyExpr()
            .map{(visit(it) as IRNode).ir}
            .fold(topIR, { acc, ir -> acc.minus(ir).unwrap() })

    return IRNode(ir, this.datamap)
  }
}