package io.nodle.substratesdk.rpc

import com.neovisionaries.ws.client.*
import io.nodle.substratesdk.utils.onDebugOnly
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import org.json.JSONException
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * @author Lucien Loiseau on 28/05/20.
 */
class WebSocketRpc(private val substrateRpcUrl: Array<out String>) {

    private val log: Logger = LoggerFactory.getLogger(WebSocketRpc::class.java)
    private val lock: ReentrantLock = ReentrantLock()

    private var ws: WebSocket? = null
    private var cmdId: Int = 1
    private val recvChannel = BehaviorSubject.create<JSONObject>()

    private val webSocketListener: WebSocketListener = object : WebSocketAdapter() {
        override fun onTextMessage(websocket: WebSocket?, text: String?) {
            try {
                val json = JSONObject(text!!)
                onDebugOnly { log.debug("substrate rpc < $json -- from thread ${Thread.currentThread().name} )") }
                recvChannel.onNext(json)
            } catch (e: JSONException) {
                // ignore
            }
        }
    }

    @Throws(WebSocketException::class)
    private fun checkOpen() {
        lock.withLock {
            if ((ws == null) || !ws!!.isOpen) {
                open()
            }
        }
    }

    @Throws(WebSocketException::class)
    private fun open() {
        close()
        substrateRpcUrl.forEachIndexed { index, url ->
            try {
                ws = WebSocketFactory().createSocket(url)
                ws?.pingInterval = 1000
                ws?.addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                ws?.connect()
                ws?.addListener(webSocketListener)
                return

            } catch (e: Exception) {
                onDebugOnly { log.error("EXCEPTION > ${e.printStackTrace()}") }
                if (index == substrateRpcUrl.size - 1) {
                    throw e
                }
                // Else do nothing. Continue to next url in loop.
            }
        }
    }

    private fun close() {
        try {
            ws?.removeListener(webSocketListener)
            ws?.disconnect()
            ws = null
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun <T> getResponse(queryId: Int): Single<T> {
        return recvChannel
            .filter { it.getInt("id") == queryId }
            .map {
                if (it.has("error")) {
                    throw Exception(it.getJSONObject("error").toString())
                }
                @Suppress("UNCHECKED_CAST") // if it fails it throws an exception
                it.get("result") as T
            }
            .firstOrError()
    }

    fun <T> send(method: RpcMethod): Single<T> {
        return Single
            .just(cmdId++)
            .map {
                checkOpen()
                val json = json {
                    "id" to it
                    "jsonrpc" to "2.0"
                    "method" to method.method
                    "params" to method.params
                }
                onDebugOnly { log.debug("substrate rpc > $json") }
                ws?.sendText(json.toString())
                it
            }
            .flatMap {
                getResponse<T>(it)
            }
    }
}


