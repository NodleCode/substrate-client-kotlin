package io.nodle.substratesdk.types

import java.math.BigInteger

/**
 * @author Lucien Loiseau on 14/07/20.
 */
data class AccountInfo(
    var nonce: UInt,
    var refCount: UInt,
    var data: AccountData
)

data class AccountData(
    var free: BigInteger,
    var reserved: BigInteger,
    var miscFrozen: BigInteger,
    var feeFrozen: BigInteger)