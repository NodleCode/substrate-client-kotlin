package io.nodle.substratesdk.account

import org.bouncycastle.crypto.digests.Blake2bDigest
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.komputing.kbase58.decodeBase58
import org.komputing.kbase58.encodeToBase58String
import java.lang.Exception

/**
 * @author Lucien Loiseau on 28/05/20.
 * REFERENCE: https://github.com/paritytech/substrate/wiki/External-Address-Format-(SS58)
 */
private val contextPrefix = "SS58PRE".toByteArray()

fun ByteArray.toSS58(substrateID: Byte = 0x2a): String {
    val body = byteArrayOf(substrateID) + this

    // documentation says blake2b-256 but reference implementation use 512 bits
    // https://github.com/paritytech/substrate/blob/master/primitives/core/src/crypto.rs
    val b2b = Blake2bDigest(512)
    val checksum = ByteArray(b2b.digestSize)
    b2b.update(contextPrefix, 0, contextPrefix.size)
    b2b.update(body, 0, body.size)
    b2b.doFinal(checksum, 0)

    return (body + checksum.slice(0..1)).encodeToBase58String()
}

fun String.ss58ToBa(): ByteArray = decodeBase58().drop(1).dropLast(2).toByteArray()