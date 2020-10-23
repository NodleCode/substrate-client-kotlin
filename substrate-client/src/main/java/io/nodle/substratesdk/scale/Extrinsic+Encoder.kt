package io.nodle.substratesdk.scale

import io.nodle.substratesdk.types.*
import java.lang.Long.max
import java.nio.ByteBuffer
import java.nio.ByteOrder

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
    val encoded = (trailingZeros - 1).coerceAtLeast(1).coerceAtMost(15) + (((phase / quantizeFactor) shl 4))
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
            this.ed25519
}

fun ExtrinsicSignature.toU8a(): ByteArray {
    return byteArrayOf(0xff.toByte()) +
            signer.encoded +
            signature.toU8a() +
            era.toU8a() +
            nonce.toCompactU8a() +
            tip.toCompactU8a()
}

fun Extrinsic.toU8a(): ByteArray {
    val payload = byteArrayOf(0x84.toByte()) + signature.toU8a() + method.toU8a()
    return payload.size.toCompactU8a() + payload
}
