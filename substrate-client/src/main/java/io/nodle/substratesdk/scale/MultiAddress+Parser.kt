package io.nodle.substratesdk.scale

import io.nodle.substratesdk.types.*
import java.nio.ByteBuffer

/**
 * @author Lucien Loiseau on 09/04/21.
 */

@ExperimentalUnsignedTypes
fun ByteBuffer.readMultiAddress(): MultiAddress =
    when (val type = this.readU8().toInt()) {
        AddressType.AccountID.id -> {
            val ba = ByteArray(32)
            get(ba)
            AccountIDAddress(ba)
        }
        AddressType.Raw.id -> RawAddress(readByteArray())
        AddressType.Address20.id -> {
            val ba = ByteArray(20)
            get(ba)
            Address20(ba)
        }
        AddressType.Address32.id -> {
            val ba = ByteArray(32)
            get(ba)
            Address32(ba)
        }
        else -> throw ScaleParserException("unsupported multiaddress type: ${type}")
    }