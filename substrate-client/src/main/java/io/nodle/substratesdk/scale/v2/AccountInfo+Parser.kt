package io.nodle.substratesdk.scale.v2

import io.nodle.substratesdk.scale.readU128
import io.nodle.substratesdk.scale.readU32
import io.nodle.substratesdk.types.AccountData
import io.nodle.substratesdk.types.AccountInfo
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * @author Lucien Loiseau on 30/07/20.
 */

class ScaleEncoderException(msg: String?) : Exception(msg)

// with the release of Substrate 2.0, the refCount is represented as u32 instead of u8
// Substrate v2.0.0 https://github.com/paritytech/substrate/releases/tag/v2.0.0
// Refcounts are now u32 (#7164) https://github.com/paritytech/substrate/pull/7164
fun ByteBuffer.readAccountInfoV12(): AccountInfo {
    return AccountInfo(
        readU32(),
        readU32(),
        0.toUInt(),
        AccountData(
            readU128(),
            readU128(),
            readU128(),
            readU128()
        )
    )
}