package io.nodle.substratesdk.scale.v1

import io.nodle.substratesdk.scale.readU128
import io.nodle.substratesdk.scale.readU32
import io.nodle.substratesdk.scale.readU8
import io.nodle.substratesdk.types.AccountData
import io.nodle.substratesdk.types.AccountInfo
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * @author Lucien Loiseau on 09/04/21.
 */

fun ByteBuffer.readAccountInfoV1(): AccountInfo {
    return AccountInfo(
        readU32(),
        readU8().toUInt(),
        0.toUInt(),
        AccountData(
            readU128(),
            readU128(),
            readU128(),
            readU128()
        )
    )
}