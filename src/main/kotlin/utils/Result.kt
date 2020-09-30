package utils;

class ResultException(message: String) : Exception(message)

sealed class Result<out T, out E>

class Ok<out T>(val value: T): Result<T, Nothing>()

class Err<out E>(val error: E): Result<Nothing, E>()

fun <U, T, E> Result<T, E>.map(transform: (T) -> U): Result<U, E> =
  when (this) {
    is Ok -> Ok(transform(value))
    is Err -> this
  }

fun <U, T, E> Result<T, E>.andThen(transform: (T) -> Result<U, E>): Result<U, E> =
  when (this) {
    is Ok -> transform(value)
    is Err -> this
  }

fun <T, E> Result<T, E>.unwrap(): T =
  when (this) {
    is Ok -> value
    is Err -> throw ResultException(error.toString())
  }