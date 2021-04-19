package mock;

class PosetElement(val label: String){
  companion object {
    val validChars = 'A'.until('Z'+1) + 'a'.until('z'+1)
  }
  private val children = mutableListOf<PosetElement>()

}