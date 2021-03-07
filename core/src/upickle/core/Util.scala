package upickle.core

object Util {
  def hexString(i: Int) = hexStrings(i)
  private[this] val hexStrings = Array[String](
    "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0a", "0b", "0c", "0d", "0e", "0f",
    "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1a", "1b", "1c", "1d", "1e", "1f",
    "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "2a", "2b", "2c", "2d", "2e", "2f",
    "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "3a", "3b", "3c", "3d", "3e", "3f",
    "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4a", "4b", "4c", "4d", "4e", "4f",
    "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "5a", "5b", "5c", "5d", "5e", "5f",
    "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "6a", "6b", "6c", "6d", "6e", "6f",
    "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "7a", "7b", "7c", "7d", "7e", "7f",
    "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "8a", "8b", "8c", "8d", "8e", "8f",
    "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "9a", "9b", "9c", "9d", "9e", "9f",
    "a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7", "a8", "a9", "aa", "ab", "ac", "ad", "ae", "af",
    "b0", "b1", "b2", "b3", "b4", "b5", "b6", "b7", "b8", "b9", "ba", "bb", "bc", "bd", "be", "bf",
    "c0", "c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8", "c9", "ca", "cb", "cc", "cd", "ce", "cf",
    "d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9", "da", "db", "dc", "dd", "de", "df",
    "e0", "e1", "e2", "e3", "e4", "e5", "e6", "e7", "e8", "e9", "ea", "eb", "ec", "ed", "ee", "ef",
    "f0", "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "fa", "fb", "fc", "fd", "fe", "ff"
  )
  def hexChar(i: Int) = hexChars(i)
  private[this] final val hexChars: Array[Int] = {
    val arr = new Array[Int](128)
    var i = 0
    while (i < 10) { arr(i + '0') = i; i += 1 }
    i = 0
    while (i < 16) { arr(i + 'a') = 10 + i; arr(i + 'A') = 10 + i; i += 1 }
    arr
  }


  def bytesToString(bs: Array[Byte]) = bs.map(b => hexString(b & 0xFF)).mkString("-")
  def stringToBytes(s: String) = s.split('-').map(Integer.parseInt(_, 16).toByte)

  def parseIntegralNum(s: CharSequence, decIndex: Int, expIndex: Int, index: Int) = {
    val expMul =
      if (expIndex == -1) 1
      else{
        var mult = 1
        val e = parseLong(s, expIndex + 1, s.length())
        var i = 0
        while(i < e){
          if (mult >= Long.MaxValue / 10) throw new Abort("expected integer")
          mult = mult * 10
          i += 1
        }
        mult
      }

    val intPortion = {
      val end =
        if(decIndex != -1) decIndex
        else if (expIndex != -1) expIndex
        else s.length

      parseLong(s, 0, end) * expMul
    }

    val decPortion =
      if (decIndex == -1) 0
      else{
        val end = if(expIndex != -1) expIndex else s.length
        var value = parseLong(s, decIndex + 1, end) * expMul
        var i = end - (decIndex + 1)
        while(i > 0) {
          value = value / 10
          i -= 1
        }
        if (s.charAt(0) == '-') -value else value
      }

    intPortion + decPortion
  }
  def parseLong(cs: CharSequence, start: Int, len: Int): Long = {

    // we store the inverse of the positive sum, to ensure we don't
    // incorrectly overflow on Long.MinValue. for positive numbers
    // this inverse sum will be inverted before being returned.
    var inverseSum: Long = 0L
    var inverseSign: Long = -1L
    var i: Int = start

    if (cs.charAt(start) == '-') {
      inverseSign = 1L
      i = 1
    }

    val size = len - i
    if (i >= len || size > 19) throw new NumberFormatException(cs.toString)

    while (i < len) {
      val digit = cs.charAt(i).toInt - 48
      if (digit < 0 || 9 < digit) new NumberFormatException(cs.toString)
      inverseSum = inverseSum * 10L - digit
      i += 1
    }

    // detect and throw on overflow
    if (size == 19 && (inverseSum >= 0 || (inverseSum == Long.MinValue && inverseSign < 0))) {
      throw new NumberFormatException(cs.toString)
    }

    inverseSum * inverseSign
  }

  def reject(j: Int): PartialFunction[Throwable, Nothing] = {
    case e: Abort =>
      throw new AbortException(e.msg, j, -1, -1, e)
  }
}
