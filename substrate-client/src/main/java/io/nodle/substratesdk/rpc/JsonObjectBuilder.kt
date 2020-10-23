package io.nodle.substratesdk.rpc

import org.json.JSONObject
import java.util.*

/**
 * @author Lucien Loiseau on 14/07/20.
 */
class JsonObjectBuilder {
    private val deque: Deque<JSONObject> = LinkedList()

    fun json(build: JsonObjectBuilder.() -> Unit): JSONObject {
        deque.push(JSONObject())
        this.build()
        return deque.pop()
    }

    infix fun <T> String.to(value: T) {
        deque.peek().put(this, value)
    }
}

public fun json(build: JsonObjectBuilder.() -> Unit): JSONObject {
    return JsonObjectBuilder().json(build)
}
