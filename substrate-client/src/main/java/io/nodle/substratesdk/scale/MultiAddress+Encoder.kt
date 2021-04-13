package io.nodle.substratesdk.scale

import io.nodle.substratesdk.types.*

/**
 * @author Lucien Loiseau on 09/04/21.
 */

fun MultiAddress.toU8a(): ByteArray {
    return type.id.toByte().toU8a() +
            when (type) {
                AddressType.AccountID -> (this as AccountIDAddress).pubkey
                AddressType.Raw -> (this as RawAddress).bytes.toU8a()
                AddressType.Address20 -> (this as Address20).bytes
                AddressType.Address32 -> (this as Address32).bytes
                else -> throw ScaleParserException("unsupported multiaddress type: ${type.name}")
            }
}