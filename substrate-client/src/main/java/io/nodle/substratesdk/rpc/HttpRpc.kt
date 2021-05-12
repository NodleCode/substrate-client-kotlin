package io.nodle.substratesdk.rpc

import io.nodle.substratesdk.utils.onDebugOnly
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.HttpURLConnection
import java.net.URL

/**
 * @author Lucien Loiseau on 12/05/21.
 */
class HttpRpc(private val substrateRpcUrl: Array<out String>) : SubstrateRpc {
    private val log: Logger = LoggerFactory.getLogger(HttpRpc::class.java)

    private fun open() : HttpURLConnection? {
        substrateRpcUrl.forEachIndexed { _, url ->
            try {
                return (URL(url).openConnection() as HttpURLConnection)
            } catch (e: Exception) {
                // iterate with next url
            }
        }
        return null
    }

    override fun <T> send(method: RpcMethod): Single<T> {
        return Observable
            .fromIterable(substrateRpcUrl.asIterable())
            .map { url ->
                try {
                    val connection = (URL(url).openConnection() as HttpURLConnection)
                    val json = json {
                        "id" to 1
                        "jsonrpc" to "2.0"
                        "method" to method.method
                        "params" to method.params
                    }
                    onDebugOnly { log.debug("substrate rpc > $json") }

                    connection.requestMethod = "POST"
                    connection.doInput = true
                    connection.doOutput = true
                    connection.connectTimeout = 10000
                    connection.readTimeout = 10000
                    connection.instanceFollowRedirects = true
                    connection.connect()
                    val out = connection.outputStream

                    // send bundle
                    out.write(json.toString().toByteArray())

                    // return response code
                    if (connection.responseCode == HttpURLConnection.HTTP_ACCEPTED ||
                        connection.responseCode == HttpURLConnection.HTTP_OK) {
                        // response may contain multiple bundle
                        val response = connection.inputStream.readBytes().toString()
                        JSONObject(response)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }
            .filter { it != null }
            .map { it as JSONObject }
            .firstOrError()
            .map {
                if (it.has("error")) {
                    throw Exception(it.getJSONObject("error").toString())
                }
                @Suppress("UNCHECKED_CAST") // if it fails it throws an exception
                it.get("result") as T
            }
    }

}