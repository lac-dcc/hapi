package hapi

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree

import java.io.FileInputStream
import java.io.InputStream

import HapiLexer
import HapiParser

import utils.*

sealed class IR {
  companion object {}
  abstract override fun toString(): String
  abstract fun empty(): Boolean
}

class Terminal (val terminal: Set<String>): IR () {
  override fun toString(): String = this.terminal.toString()
  override fun empty(): Boolean = this.terminal.isEmpty()
}

class NonTerminal(val nonTerminal: Map<String, IR>) : IR () {
  override fun toString(): String = this.nonTerminal.toString()
  override fun empty(): Boolean = this.nonTerminal.isEmpty()
}

fun IR.minus(otherIR: IR): Result<IR, String> =
  when {
    this is Terminal && otherIR is Terminal -> this.minus(otherIR)
    this is NonTerminal && otherIR is NonTerminal -> this.minus(otherIR)
    else -> Err("Can't subtract IRs of different nesting levels")
  }

fun IR.plus(otherIR: IR): Result<IR, String> =
  when {
    this is Terminal && otherIR is Terminal -> this.plus(otherIR)
    this is NonTerminal && otherIR is NonTerminal -> this.plus(otherIR)
    else -> Err("Can't sum IRs of different nesting levels")
  }

fun IR.Companion.from(attrs: Map<String, Set<String>>, priority: List<String>): IR =
  when (priority.size) {
    1 -> Terminal(attrs[priority[0]]!!)
    else -> NonTerminal(
      attrs[priority[0]]!!.associateWith({ 
        IR.from(attrs, priority.drop(1)) 
      })
    )
  }

fun IR.Companion.generate(
  file: String,
  priority: List<String> ): ASTNode = 
  FileInputStream(file).let {
      val input = CharStreams.fromStream(it)
      val lexer = HapiLexer(input)
      val tokens = CommonTokenStream(lexer)
      val parser = HapiParser(tokens)
      val tree = parser.program()

      val eval = Visitor(file, priority)
      eval.visit(tree)
    }

fun Terminal.minus(other: Terminal): Result<Terminal, String> = 
  Ok(Terminal(this.terminal.subtract(other.terminal)))

// @TODO: a better name
fun subtract(nt1: Map<String, IR>, nt2: Map<String, IR>, k: String): Result<NonTerminal, String> = 
  nt1[k]!!.minus(nt2[k]!!).let {
    when (it) {
      is Ok<*> -> Ok(NonTerminal(
        if (it.unwrap().empty())
          nt1 - k 
        else 
          nt1 + Pair(k, it.unwrap())
      ))
      is Err<*> -> it as Result<NonTerminal, String>
    }
  }

fun NonTerminal.minus(other: NonTerminal): Result<NonTerminal, String> = 
  this.nonTerminal.keys.fold(Ok(this) as Result<NonTerminal, String>, { acc, k ->
    when {
      acc is Ok<*> && other.nonTerminal.containsKey(k) -> 
        subtract(acc.unwrap().nonTerminal, other.nonTerminal, k)
      else -> acc
    }
  })

fun Terminal.plus(other: Terminal): Result<IR, String> =
  Ok(Terminal(this.terminal.union(other.terminal)))

// @TODO: a better name
fun sum(nt1: Map<String, IR>, nt2: Map<String, IR>, k: String): Result<NonTerminal, String> = 
  nt1[k]!!.plus(nt2[k]!!).let{
    when (it){
      is Ok<*> ->  Ok(NonTerminal(
        if (nt1.containsKey(k))
          nt1 + Pair(k, it.unwrap())
        else
          nt1 + Pair(k, nt2[k]!!)
      ))
      is Err<*> -> it as Result<NonTerminal, String>
    }
    
  }

fun NonTerminal.plus(other: NonTerminal): Result<IR, String> = 
    other.nonTerminal.keys.fold(Ok(this) as Result<NonTerminal, String>, { acc, k ->
      when {
        acc is Ok<*> ->
          if (acc.unwrap().nonTerminal.containsKey(k)){
            sum(acc.unwrap().nonTerminal, other.nonTerminal, k)
          } else {
            Ok(NonTerminal(acc.unwrap().nonTerminal + Pair(k, other.nonTerminal[k]!!)))
          }
        else -> acc
      }
    })