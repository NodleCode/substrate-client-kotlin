package io.nodle.substratesdk.types

import io.nodle.substratesdk.account.Account
import io.nodle.substratesdk.rpc.SubstrateProvider
import io.reactivex.rxjava3.core.Single
import java.lang.Exception
import java.math.BigInteger

/**
 * @author Lucien Loiseau on 29/06/20.
 */

abstract class ExtrinsicCall

data class TransferCall(
    val moduleIndex: Int,
    val callIndex: Int,
    val destAccount: Account,
    val amount: BigInteger
) : ExtrinsicCall()

class CallNotFoundException(msg: String?) : Exception(msg)