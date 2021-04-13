package io.nodle.substratesdk.scale.v2

import io.nodle.substratesdk.scale.ScaleEncoderException
import io.nodle.substratesdk.scale.toCompactU8a
import io.nodle.substratesdk.scale.toU8a
import io.nodle.substratesdk.types.*

fun Extrinsic.toU8aV2(): ByteArray {
    val payload = byteArrayOf(0x84.toByte()) + signature.toU8aV2() + method.toU8aV2()
    return payload.size.toCompactU8a() + payload
}

fun ExtrinsicSignature.toU8aV2(): ByteArray {
    if (signer.type != AddressType.AccountID) {
        throw ScaleEncoderException("address type not supported")
    }

    return byteArrayOf(0xff.toByte()) +
            (signer as AccountIDAddress).pubkey +
            signature.toU8a() +
            era.toU8a() +
            nonce.toCompactU8a() +
            tip.toCompactU8a()
}

fun ExtrinsicPayload.toU8aV2(): ByteArray {
    return method.toU8aV2() +
            era.toU8a() +
            nonce.toCompactU8a() +
            tip.toCompactU8a() +
            specVersion.toInt().toU8a() +
            transactionVersion.toInt().toU8a() +
            genesisHash +
            blockHash
}