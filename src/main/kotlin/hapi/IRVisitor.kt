package hapi

import java.io.File

import utils.*

import HapiBaseVisitor
import HapiParser

const val entryPoint = "main"

typealias Environment = HashMap<String, IR>

sealed class ASTNode {}
data class EnvNode (val env: Environment): ASTNode() {}
data class IRNode (val ir: IR): ASTNode() {}

class IRVisitor(
  val root: String,
  val datamap: DataMap,
  val priority: List<String>    // @TODO: think of a way to remove this
  ) : HapiBaseVisitor<ASTNode>() {

  val env: Environment = hashMapOf()
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
    for (stmt in ctx.stmt())
      visit(stmt).let {
        if (it != null)
          this.env.putAll((it as EnvNode).env)
      }

    val main = this.env.get(this.namespace + "::" + entryPoint)
    if (main != null)
      return IRNode(main)
    else
      throw Exception("no entry point 'main' provided")
  }

  override fun visitLibrary(ctx: HapiParser.LibraryContext ): ASTNode {
    this.namespace = ctx.exportStmt().ID().toString()

    for (stmt in ctx.stmt())
      visit(stmt).let {
        if (it != null)
          this.env.putAll((it as EnvNode).env)
      }

    return EnvNode(this.env)
  }

  override fun visitImportStmt(ctx: HapiParser.ImportStmtContext): ASTNode {
    val module = this.root + "/" + ctx.ID().toString() + ".hp"

    return File(module).let {
      val ast = evalIR(it.readText(), this.root, this.datamap, this.priority)

      when (ast) {
        is EnvNode -> ast
        else -> throw Exception("can't import an executable file")
      }
    }
  }

  override fun visitLetStmt(ctx: HapiParser.LetStmtContext): ASTNode {
    val id = ctx.ID().toString()
    val key = this.namespace + "::" + id
    val ir = (visit(ctx.policyExpr()) as IRNode).ir
    val env: Environment = hashMapOf(key to ir)
    return EnvNode(env)
  }

  fun inferTypeFrom(ruleIndex: Int): IRType {
    if (ruleIndex == HapiParser.RULE_allowExpr || ruleIndex == HapiParser.RULE_allowExceptExpr)
      return IRType.ALLOW
    else
      return IRType.DENY
  }

  override fun visitAttributeExpr(ctx: HapiParser.AttributeExprContext): ASTNode {
    val type =  inferTypeFrom(ctx.getParent().getRuleIndex())
    return IRNode(IR.from(attrsFrom(ctx.attribute()), type, this.priority))
  }

  override fun visitLiteralExpr(ctx: HapiParser.LiteralExprContext): ASTNode {
    var literal = ctx.ID().map {e -> e.toString()}.toMutableList()
    if (literal.size == 1)
      literal.add(0, this.namespace)
    
    val name = literal.joinToString(separator="::")
    val ir = this.env.get(name)

    if (ir != null)
      return IRNode(ir)
    else
      throw Exception("undefined name: ${name}")
  }

  override fun visitDenyExceptExpr(ctx: HapiParser.DenyExceptExprContext): ASTNode {
    val denyAttr = visit(ctx.attributeExpr())
    val denyAttrIR = (denyAttr as IRNode).ir

    val ir = ctx.allowExpr()
            .map{(visit(it) as IRNode).ir}
            .fold(denyAttrIR, { acc, ir -> acc.minus(ir).unwrap() })
    return IRNode(ir)
  }

  override fun visitDenyAllExceptExpr(ctx: HapiParser.DenyAllExceptExprContext): ASTNode {

    val top = this.datamap.mapValues { setOf<String>() }
    val topIR = IR.from(top, IRType.ALLOW, this.priority)
    
    val ir = ctx.allowExpr()
              .map{(visit(it) as IRNode).ir}
              .fold(topIR, { acc, ir -> acc.plus(ir).unwrap() })
    return IRNode(ir)
  }

  override fun visitAllowExceptExpr(ctx: HapiParser.AllowExceptExprContext): ASTNode {

    val allowAttr = visit(ctx.attributeExpr())
    val allowAttrIR = (allowAttr as IRNode).ir

    val ir = ctx.denyExpr()
            .map{(visit(it) as IRNode).ir}
            .fold(allowAttrIR, { acc, ir -> acc.minus(ir).unwrap() })
    return IRNode(ir)
  }

  override fun visitAllowAllExceptExpr(ctx: HapiParser.AllowAllExceptExprContext): ASTNode {

    val top = this.datamap.mapValues { (_, lattice) -> lattice.atoms(lattice.TOP) }
    val topIR = IR.from(top, IRType.ALLOW, this.priority)

    val ir = ctx.denyExpr()
            .map{(visit(it) as IRNode).ir}
            .fold(topIR, { acc, ir -> acc.minus(ir).unwrap() })

    return IRNode(ir)
  }
}