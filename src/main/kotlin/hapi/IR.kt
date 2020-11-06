package hapi

import utils.*

enum class IRType {
    ALLOW {
      override fun complement() = DENY
    },
    DENY {
      override fun complement() = ALLOW
    };

    abstract fun complement(): IRType
}

sealed class IR (val type: IRType) {
  companion object {}
  abstract override fun toString(): String
  abstract fun empty(): Boolean
}

class Terminal (val terminal: Set<String>, type: IRType): IR (type) {
  override fun toString(): String = this.terminal.toString()
  override fun empty(): Boolean = this.terminal.isEmpty()
}

class NonTerminal(val nonTerminal: Map<String, IR>, type: IRType) : IR (type) {
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

fun IR.Companion.from(attrs: Map<String, Set<String>>, type: IRType, keys: List<String>): IR =
  when (keys.size) {
    1 -> Terminal(attrs[keys[0]]!!, type)
    else -> NonTerminal(
      attrs[keys[0]]!!.associateWith({
        IR.from(attrs, type, keys.drop(1)) 
      }), type
    )
  }

fun Terminal.minus(other: Terminal): Result<Terminal, String> = 
  if (this.type == other.type)
    Err("expected type " + this.type.complement() + " got " + this.type)
  else
    Ok(Terminal(this.terminal.subtract(other.terminal), this.type))

// @TODO: a better name
fun subtract(nt1: Map<String, IR>, nt2: Map<String, IR>, k: String): Result<Map<String, IR>, String> = 
  nt1[k]!!.minus(nt2[k]!!).let {
    when (it) {
      is Ok<*> -> Ok(
        if (it.unwrap().empty())
          nt1 - k 
        else 
          nt1 + Pair(k, it.unwrap())
      )
      is Err<*> -> it as Result<Map<String, IR>, String>
    }
  }

fun NonTerminal.minus(other: NonTerminal): Result<NonTerminal, String> =
  if (this.type == other.type)
    Err("expected type " + this.type.complement() + " got " + this.type)
  else
    this.nonTerminal.keys.fold(Ok(this) as Result<NonTerminal, String>, { acc, k ->
      when {
        acc is Ok<*> && other.nonTerminal.containsKey(k) -> 
          subtract(acc.unwrap().nonTerminal, other.nonTerminal, k).map{ NonTerminal(it, this.type) }
        else -> acc
      }
    })

fun Terminal.plus(other: Terminal): Result<IR, String> =
  if (this.type != other.type)
    Err("expected type " + this.type + " got " + other.type)
  else
    Ok(Terminal(this.terminal.union(other.terminal), this.type))

// @TODO: a better name
fun sum(nt1: Map<String, IR>, nt2: Map<String, IR>, k: String): Result<Map<String, IR>, String> = 
  nt1[k]!!.plus(nt2[k]!!).let{
    when (it){
      is Ok<*> ->  Ok(
        if (nt1.containsKey(k))
          nt1 + Pair(k, it.unwrap())
        else
          nt1 + Pair(k, nt2[k]!!)
      )
      is Err<*> -> it as Result<Map<String, IR>, String>
    }
    
  }

fun NonTerminal.plus(other: NonTerminal): Result<IR, String> =
  if (this.type != other.type)
    Err("expected type " + this.type + " got " + other.type)
  else
    other.nonTerminal.keys.fold(Ok(this) as Result<NonTerminal, String>, { acc, k ->
      when {
        acc is Ok<*> ->
          if (acc.unwrap().nonTerminal.containsKey(k)){
            sum(acc.unwrap().nonTerminal, other.nonTerminal, k).map{ NonTerminal(it, this.type) }
          } else {
            Ok(NonTerminal(acc.unwrap().nonTerminal + Pair(k, other.nonTerminal[k]!!), this.type))
          }
        else -> acc
      }
    })