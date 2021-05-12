package io.nodle.substratesdk.rpc

import io.reactivex.rxjava3.core.Single

/**
 * @author Lucien Loiseau on 12/05/21.
 */
interface SubstrateRpc {

    fun <T> send(method: RpcMethod): Single<T>

}