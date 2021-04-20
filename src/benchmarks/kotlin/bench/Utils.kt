package bench

fun nextLexicographString(word: String): String {
  var result = word
  if (word.count() > 0){
    var lastCh = word.takeLast(1).last()
    if (lastCh == '_') { 
      result += "A"
    } else if(lastCh >= 'A' && lastCh < 'z'){
      result = result.dropLast(1) + (lastCh+1)
    } else {
      result += "_A"
    }
    return result
  } else {
    return "A"
  }
}