package io.nodle.substratesdk

import io.nodle.substratesdk.account.Account
import io.nodle.substratesdk.account.Wallet
import io.nodle.substratesdk.account.signMsg
import io.nodle.substratesdk.rpc.AuthorSubmitExtrinsic
import io.nodle.substratesdk.rpc.PaymentQueryInfo
import io.nodle.substratesdk.rpc.StateGetStorage
import io.nodle.substratesdk.rpc.SubstrateProvider
import io.nodle.substratesdk.scale.*
import io.nodle.substratesdk.scale.v1.readAccountInfoV1
import io.nodle.substratesdk.scale.v2.readAccountInfoV12
import io.nodle.substratesdk.types.*
import io.nodle.substratesdk.utils.*
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.functions.Function5
import org.json.JSONObject
import java.math.BigInteger
import java.nio.ByteBuffer

/**
 * @author Lucien Loiseau on 14/07/20.
 */
@ExperimentalUnsignedTypes
fun Account.getAccountInfo(provider: SubstrateProvider): Single<AccountInfo> {
    return provider.getMetadata()
        .flatMap { metadata ->
            val ba = toU8a()
            val key = "System".xxHash128() + "Account".xxHash128() + ba.blake128() + ba
            provider.rpc.send<String>(StateGetStorage("0x" + key.toHex())).map { scale ->
                when (metadata.version) {
                    in 0..11 -> ByteBuffer.wrap(scale.hexToBa()).readAccountInfoV1()
                    else -> ByteBuffer.wrap(scale.hexToBa()).readAccountInfoSub3()
                }
            }
        }
}

@ExperimentalUnsignedTypes
fun Account.getBalance(provider: SubstrateProvider): Single<BigInteger> {
    return getAccountInfo(provider)
        .map { it.data.free }
}

@ExperimentalUnsignedTypes
fun Wallet.signTx(
    provider: SubstrateProvider,
    destAccount: Account,
    amount: BigInteger,
    era: ExtrinsicEra = ImmortalEra()
): Single<Extrinsic> {
    return Single.zip(
        provider.transferCall(destAccount, amount),
        provider.getGenesisHash(),
        provider.getSpecVersion(),
        provider.getTransactionVersion(),
        getAccountInfo(provider),
        Function5 { call, genesisHash, specVersion, transactionVersion, sourceWalletInfo ->
            val payload = ExtrinsicPayload(
                genesisHash.hexToBa(),
                era,
                genesisHash.hexToBa(),
                call,
                sourceWalletInfo.nonce.toLong(),
                specVersion,
                BigInteger.ZERO,
                transactionVersion
            )
            val signature = privateKey.signMsg(payload.toU8a(provider))
            val extrinsicSignature = ExtrinsicSignature(
                AccountIDAddress(privateKey.generatePublicKey()),
                ExtrinsicEd25519Signature(signature),
                era,
                sourceWalletInfo.nonce.toLong(),
                BigInteger.ZERO
            )
            Extrinsic(
                extrinsicSignature,
                call
            )
        }
    )
}

fun Extrinsic.estimateFee(provider: SubstrateProvider): Single<BigInteger> {
    return provider.rpc
        .send<JSONObject>(PaymentQueryInfo("0x" + toU8a(provider).toHex()))
        .map { it.getString("partialFee").toBigInteger() }
}

fun Extrinsic.send(provider: SubstrateProvider): Single<String> {
    return provider.rpc.send(AuthorSubmitExtrinsic("0x" + toU8a(provider).toHex()))
}

@ExperimentalUnsignedTypes
fun Wallet.signAndSend(
    provider: SubstrateProvider,
    destAccount: Account,
    amount: BigInteger,
    era: ExtrinsicEra = ImmortalEra()
): Single<String> {
    return this
        .signTx(provider, destAccount, amount, era)
        .flatMap { it.send(provider) }
}

fun getVersion(): String = GIT_SHA