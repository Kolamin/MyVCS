package vcs

import java.math.BigInteger
import java.security.MessageDigest

fun main() {
    val digester = MessageDigest.getInstance("SHA1")
    val input = "Secret string".toByteArray()
    val digest = digester.digest(input)
    val genHash = digest.joinToString("") { "%02x".format(it) }
    println(genHash)
}


//
//fun stringToSHA(input:String): String {
//    val md = MessageDigest.getInstance("SHA1")
//    return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
//}