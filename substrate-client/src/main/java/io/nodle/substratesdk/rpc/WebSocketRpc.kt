package io.nodle.substratesdk.rpc

import com.neovisionaries.ws.client.*
import io.nodle.substratesdk.utils.onDebugOnly
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONException
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * @author Lucien Loiseau on 28/05/20.
 */
class WebSocketRpc(private val url: String) : ISubstrateRpc {
    private val log: Logger = LoggerFactory.getLogger(WebSocketRpc::class.java)
    private val lock: ReentrantLock = ReentrantLock()

    private var ws: WebSocket? = null
    private var cmdId: Int = 1
    private var recvChannel = BehaviorSubject.create<JSONObject>()

    private val webSocketListener: WebSocketListener = object : WebSocketAdapter() {
        private var timeout: Job? = null
        private val mutex = Mutex()

        private fun resetTimeout() {
            runBlocking {
                mutex.withLock {
                    timeout?.cancel()
                    timeout = CoroutineScope(Dispatchers.Default).launch {
                        delay(20000)
                        if (isActive) {
                            onDebugOnly { log.debug("rpc ($url) -- timeout fired, closing websocket") }
                            timeout = null
                            close()
                        }
                    }
                }
            }
        }

        override fun onConnected(
            websocket: WebSocket?,
            headers: MutableMap<String, MutableList<String>>?
        ) {
            super.onConnected(websocket, headers)
            resetTimeout()
        }

        override fun onFrameSent(websocket: WebSocket?, frame: WebSocketFrame?) {
            super.onFrameSent(websocket, frame)
            resetTimeout()
        }

        override fun onFrame(websocket: WebSocket?, frame: WebSocketFrame?) {
            super.onFrame(websocket, frame)
            resetTimeout()
        }

        override fun onDisconnected(
            websocket: WebSocket?,
            serverCloseFrame: WebSocketFrame?,
            clientCloseFrame: WebSocketFrame?,
            closedByServer: Boolean
        ) {
            close()
        }

        override fun onTextMessage(websocket: WebSocket?, text: String?) {
            try {
                val json = JSONObject(text!!)
                onDebugOnly { log.debug("rpc ($url) < $json -- from thread ${Thread.currentThread().name} )") }
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

    @Throws(Exception::class)
    private fun open() {
        close()
        ws = WebSocketFactory()
            .setConnectionTimeout(1000)
            .createSocket(url)
        ws?.pingInterval = 0
        ws?.addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
        ws?.connect()

        cmdId = 1
        recvChannel = BehaviorSubject.create()
        ws?.addListener(webSocketListener)
        return
    }

    private fun close() {
        try {
            ws?.removeListener(webSocketListener)
            ws?.disconnect()
            ws = null
            if (recvChannel?.hasObservers() == true) {
                recvChannel?.onError(IOException("rpc ($url) -- websocket disconnected"))
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun <T> getResponse(queryId: Int, defaultValue: T?): Single<T> {
        return recvChannel
            .filter { it.getInt("id") == queryId }
            .map {
                if (it.has("error")) {
                    throw Exception(it.getJSONObject("error").toString())
                }
                it.opt("result")?.let { result ->
                    @Suppress("UNCHECKED_CAST") // if it fails it throws an exception
                    if (!JSONObject.NULL.equals(result)) result as T
                    else defaultValue ?: throw NullJsonObjectException()
                } ?: throw Exception("query result not available")
            }
            .firstOrError()
    }

    override fun <T> send(method: RpcMethod, defaultValue: T?): Single<T> {
        return Single
            .fromCallable { checkOpen() }
            .map { cmdId++ }
            .map {
                val json = json {
                    "id" to it
                    "jsonrpc" to "2.0"
                    "method" to method.method
                    "params" to method.params
                }
                onDebugOnly { log.debug("rpc ($url)> $json") }
                ws?.sendText(json.toString())
                it
            }
            .flatMap {
                getResponse<T>(it, defaultValue)
            }
    }

    override fun url(): String {
        return url
    }
}


