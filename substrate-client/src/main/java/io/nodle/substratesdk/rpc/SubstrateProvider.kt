package io.nodle.substratesdk.rpc

import io.nodle.substratesdk.account.Account
import io.nodle.substratesdk.scale.readMetadata
import io.nodle.substratesdk.types.CallNotFoundException
import io.nodle.substratesdk.types.RuntimeMetadata
import io.nodle.substratesdk.types.TransferCall
import io.nodle.substratesdk.types.findCall
import io.nodle.substratesdk.utils.hexToBa
import io.nodle.substratesdk.utils.onDebugOnly
import io.reactivex.rxjava3.core.Single
import org.json.JSONObject
import java.math.BigInteger
import java.nio.ByteBuffer

/**
 * @author Lucien Loiseau on 29/07/20.
 */
class SubstrateProvider(vararg substrateRpcUrl: String) {
    val rpc = SubstrateRpc(substrateRpcUrl)

    var genesisHash: String? = null
    var metadata: RuntimeMetadata? = null
    var runtimeVersion: JSONObject? = null

    fun getGenesisHash(): Single<String> {
        if (genesisHash != null)
            return Single.just(genesisHash)

        return rpc.send<String>(ChainGetBlockHash(0))
            .map { it.removePrefix("0x") }
            .doOnSuccess { genesisHash = it }
    }

    @ExperimentalUnsignedTypes
    fun getMetadata(): Single<RuntimeMetadata> {
        if (metadata != null)
            return Single.just(metadata)

        return rpc.send<String>(StateGetMetadata())
            .map { it.hexToBa() }
            .map { ByteBuffer.wrap(it).readMetadata() }
            .doOnSuccess { metadata = it }
    }

    @ExperimentalUnsignedTypes
    fun transferCall(destAccount: Account, amount: BigInteger): Single<TransferCall> {
        return this.getMetadata().map {
            val call = it.findCall("Balances", "transfer")
                ?: throw CallNotFoundException("Balance transfer call is not supported on this blockchain")
            TransferCall(
                call.moduleIndex,
                call.callIndex,
                destAccount,
                amount
            )
        }
    }

    fun getRuntimeVersion(): Single<JSONObject> {
        if (runtimeVersion != null)
            return Single.just(runtimeVersion)

        return rpc.send<JSONObject>(StateGetRuntimeVersion())
            .doOnSuccess { runtimeVersion = it }
    }

    fun getSpecVersion(): Single<Long> {
        return this.getRuntimeVersion().map {
            it.getLong("specVersion")
        }
    }

    fun getTransactionVersion(): Single<Long> {
        return this.getRuntimeVersion().map {
            it.getLong("transactionVersion")
        }
    }
}