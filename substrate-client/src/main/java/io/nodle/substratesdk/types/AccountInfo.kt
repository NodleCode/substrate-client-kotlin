package io.nodle.substratesdk.types

import java.math.BigInteger

/**
 * @author Lucien Loiseau on 14/07/20.
 */
data class AccountInfo(
    var nonce: UInt,
    var consumers: UInt,
    var providers: UInt,
    var data: AccountData
)

data class AccountData(
    var free: BigInteger,
    var reserved: BigInteger,
    var miscFrozen: BigInteger,
    var feeFrozen: BigInteger
)

val nullAccountInfo = AccountInfo(
    0.toUInt(),
    0.toUInt(),
    0.toUInt(),
    AccountData(
        0.toBigInteger(),
        0.toBigInteger(),
        0.toBigInteger(),
        0.toBigInteger()
    )
)