package io.nodle.substratesdk.rpc

import io.reactivex.rxjava3.core.Single

/**
 * @author Lucien Loiseau on 12/05/21.
 */

class NullJsonObjectException : Exception()

interface ISubstrateRpc {

    fun <T> send(method: RpcMethod, defaultValue: T? = null): Single<T>

    fun url() : String

}