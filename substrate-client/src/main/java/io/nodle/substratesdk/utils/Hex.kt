package io.nodle.substratesdk.utils

/**
 * @author Lucien Loiseau on 13/08/20.
 */
/**
 * @author Lucien Loiseau on 30/07/20.
 */
fun ByteArray.toHex() =
    this.joinToString(separator = "") { it.toInt().and(0xff).toString(16).padStart(2, '0') }

fun String.hexToBa() = removePrefix("0x").pureHexToBa()

fun String.pureHexToBa() = ByteArray(this.length / 2) { this.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

fun ByteArray.trimTrailingZeros() : ByteArray = this.dropLastWhile { it == 0x00.toByte() }.toByteArray()

