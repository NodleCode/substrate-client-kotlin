package io.nodle.substratesdk.rpc

import io.nodle.substratesdk.utils.onDebugOnly
import io.reactivex.rxjava3.core.Single
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.HttpURLConnection
import java.net.URL

/**
 * @author Lucien Loiseau on 12/05/21.
 */
class HttpRpc(private val url: String) : ISubstrateRpc {
    private val log: Logger = LoggerFactory.getLogger(HttpRpc::class.java)

    override fun <T> send(method: RpcMethod, defaultValue: T?): Single<T> {
        return Single.just(url)
            .map { url ->
                val connection = (URL(url).openConnection() as HttpURLConnection)
                val json = json {
                    "id" to 1
                    "jsonrpc" to "2.0"
                    "method" to method.method
                    "params" to method.params
                }
                onDebugOnly { log.debug("rpc ($url) > $json") }

                connection.requestMethod = "POST"
                connection.doInput = true
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0"
                ) // Java/* is blocked by cloudflare
                connection.setRequestProperty("Content-Type", "application/json")
                connection.instanceFollowRedirects = true
                connection.connect()
                val out = connection.outputStream
                out.write(json.toString().toByteArray())

                if (connection.responseCode == HttpURLConnection.HTTP_ACCEPTED ||
                    connection.responseCode == HttpURLConnection.HTTP_OK
                ) {
                    val response = String(connection.inputStream.readBytes())
                    onDebugOnly { log.debug("rpc ($url) < $response") }
                    JSONObject(response)
                } else {
                    throw Exception()
                }
            }
            .map {
                if (it.has("error")) {
                    throw Exception(it.getJSONObject("error").toString())
                }

                it.opt("result")?.let { result ->
                    @Suppress("UNCHECKED_CAST") // if it fails it throws an exception
                    if (!JSONObject.NULL.equals(result)) result as T
                    else defaultValue ?: throw NullJsonObjectException()
                } ?: throw Exception("result not available")
            }
    }

    override fun url(): String {
        return url
    }

}