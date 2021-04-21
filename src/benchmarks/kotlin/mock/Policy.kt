package mock

import hapi.*

class Policy(val type: IRType,
  val productPoset: List<Pair<String, String>>) {
}