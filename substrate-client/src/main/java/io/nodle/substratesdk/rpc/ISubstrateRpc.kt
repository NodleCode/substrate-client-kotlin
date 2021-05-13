package io.nodle.substratesdk.rpc

import io.reactivex.rxjava3.core.Single

/**
 * @author Lucien Loiseau on 12/05/21.
 */
interface ISubstrateRpc {

    fun <T> send(method: RpcMethod): Single<T>

    fun url() : String

}