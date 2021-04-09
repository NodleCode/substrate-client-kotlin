package io.nodle.substratesdk.scale

import io.nodle.substratesdk.types.ExtrinsicCall
import io.nodle.substratesdk.types.TransferCall

/**
 * @author Lucien Loiseau on 30/07/20.
 */
fun ExtrinsicCall.toU8a(): ByteArray {
    return (this as? TransferCall)
        ?.toU8a()
        ?: byteArrayOf()
}

fun TransferCall.toU8a(): ByteArray {
    return byteArrayOf(moduleIndex.toByte(), callIndex.toByte()) +
            destAccount.toMultiAddress().toU8a() +
            amount.toCompactU8a()
}

