package io.nodle.substratesdk.scale.v2

import io.nodle.substratesdk.scale.toCompactU8a
import io.nodle.substratesdk.types.*

/**
 * @author Lucien Loiseau on 30/07/20.
 */

fun ExtrinsicCall.toU8aV2(): ByteArray {
    return (this as? TransferCall)
        ?.toU8aV2()
        ?: byteArrayOf()
}

fun TransferCall.toU8aV2(): ByteArray {
    return byteArrayOf(moduleIndex.toByte(), callIndex.toByte()) +
            byteArrayOf(0xff.toByte()) +
            destAccount.toU8a() +
            amount.toCompactU8a()
}
