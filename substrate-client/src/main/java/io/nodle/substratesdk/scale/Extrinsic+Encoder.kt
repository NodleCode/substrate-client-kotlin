package io.nodle.substratesdk.scale

import io.nodle.substratesdk.rpc.SubstrateProvider
import io.nodle.substratesdk.scale.v2.toU8aV2
import io.nodle.substratesdk.types.*

/**
 * @author Lucien Loiseau on 30/07/20.
 */
fun ExtrinsicEra.toU8a(): ByteArray {
    return (this as? ImmortalEra)?.toU8a()
        ?: (this as? MortalEra)?.toU8a()
        ?: byteArrayOf()
}

fun ImmortalEra.toU8a(): ByteArray {
    return byteArrayOf(0x00)
}

fun MortalEra.toU8a(): ByteArray {
    val quantizeFactor = (period shr 12).coerceAtLeast(1)
    val trailingZeros = java.lang.Long.numberOfTrailingZeros(phase)
    val encoded =
        (trailingZeros - 1).coerceAtLeast(1).coerceAtMost(15) + (((phase / quantizeFactor) shl 4))
    val first = encoded shr 8
    val second = encoded and 0xff
    return byteArrayOf(second.toByte(), first.toByte())
}

fun ExtrinsicPayload.toU8a(): ByteArray {
    return method.toU8a() +
            era.toU8a() +
            nonce.toCompactU8a() +
            tip.toCompactU8a() +
            specVersion.toInt().toU8a() +
            transactionVersion.toInt().toU8a() +
            genesisHash +
            blockHash
}

fun ExtrinsicEd25519Signature.toU8a(): ByteArray {
    return byteArrayOf(0x00) +
            this.signature
}

fun ExtrinsicSignature.toU8a(): ByteArray {
    return signer.toU8a() +
            signature.toU8a() +
            era.toU8a() +
            nonce.toCompactU8a() +
            tip.toCompactU8a()
}

fun Extrinsic.toU8a(): ByteArray {
    val payload = byteArrayOf(0x84.toByte()) + signature.toU8a() + method.toU8a()
    return payload.size.toCompactU8a() + payload
}

/**
 * BACKPORT to V2
 * --------------
 * temporary fix. Since substrate version 3, extrinsic are encoded slightly differently
 * however there is no easy and deterministic way to know which runtime version is
 * actually running. Here we use the nodle spec version but it means that this code
 * is nodle-specific and it will not work with substrate based chain using substrate 3 but
 * a specversion < 45. This is obviously a big problem which cannot be solved until the
 * chain clearly lists all its types over the RPC.
 */
fun Extrinsic.toU8a(provider: SubstrateProvider): ByteArray =
    provider.getSpecVersion().filter { it > 45 }
        ?.blockingGet()?.let { this.toU8a() }
        ?: this.toU8aV2()

fun ExtrinsicPayload.toU8a(provider: SubstrateProvider): ByteArray =
    provider.getSpecVersion().filter { it > 45 }
        ?.blockingGet()?.let { this.toU8a() }
        ?: this.toU8aV2()
