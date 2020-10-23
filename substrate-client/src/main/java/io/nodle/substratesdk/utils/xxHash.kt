package io.nodle.substratesdk.utils

import net.jpountz.xxhash.XXHashFactory
import org.bouncycastle.crypto.digests.Blake2bDigest
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * @author Lucien Loiseau on 14/07/20.
 */

/**
 * 128-bits xxhash
 *
 * -------------------------------------------------------
 * Excerpt from: https://www.shawntabrizi.com/substrate/querying-substrate-storage-via-rpc/
 *
 * ```
 * Note: Note that we use XXHash to output a 128 bit hash.
 * However, XXHash only supports 32 bit and 64 bit outputs.
 * To correctly generate the 128 bit hash, we need to hash
 * the same phrase twice, with seed 0 and seed 1, and concatenate them.
 * ```
 * -------------------------------------------------------
 */
fun ByteArray.xxHash128() : ByteArray {
    val factory = XXHashFactory.fastestInstance()
    val buf = ByteBuffer.wrap(this)

    val ret = ByteBuffer.allocate(16)
    ret.order(ByteOrder.LITTLE_ENDIAN)
    ret.putLong(factory.hash64().hash(buf, 0))
    buf.rewind()
    ret.putLong(factory.hash64().hash(buf, 1))
    return ret.array()
}

fun String.xxHash128() : ByteArray = toByteArray().xxHash128()

fun ByteArray.blake128() : ByteArray {
    val b2b = Blake2bDigest(128)
    val checksum = ByteArray(b2b.digestSize)
    b2b.update(this, 0, size)
    b2b.doFinal(checksum, 0)
    return checksum
}

fun String.blake128() : ByteArray = toByteArray().blake128()
