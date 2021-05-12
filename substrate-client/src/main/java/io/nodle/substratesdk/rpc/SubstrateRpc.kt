package io.nodle.substratesdk.rpc

import io.reactivex.rxjava3.core.Single

/**
 * @author Lucien Loiseau on 12/05/21.
 */
class SubstrateRpc(substrateUrls: Array<out String>) : ISubstrateRpc {

    private val rpcEndpoints = substrateUrls.mapNotNull { url ->
        when {
            url.startsWith("wss") -> WebSocketRpc(url)
            url.startsWith("http") -> HttpRpc(url)
            else -> null
        }
    }

    /**
     * TODO: would be nice to not do any blockingget in there
     * with flatmap
     */
    override fun <T> send(method: RpcMethod): Single<T> {
        return Single.just(rpcEndpoints)
            .map {
                it.forEach { rpc ->
                    try {
                        return@map rpc.send<T>(method).blockingGet()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                throw Exception()
            }
    }

}