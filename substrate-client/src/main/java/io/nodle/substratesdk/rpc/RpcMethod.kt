package io.nodle.substratesdk.rpc

import org.json.JSONArray

/**
 * @author Lucien Loiseau on 14/07/20.
 */
open class RpcMethod(val method: String, val params: JSONArray)

class ChainGetBlockHash(blockNumber: Int) :
    RpcMethod("chain_getBlockHash", JSONArray(arrayOf(blockNumber)))

class StateGetKeys(keyPrefix: String) :
    RpcMethod("state_getKeys", JSONArray(arrayOf(keyPrefix)))

class StateGetMetadata :
    RpcMethod("state_getMetadata", JSONArray())

class StateGetRuntimeVersion :
    RpcMethod("state_getRuntimeVersion", JSONArray())

class StateGetStorage(key: String) :
    RpcMethod("state_getStorage", JSONArray(arrayOf(key)))

class AuthorSubmitExtrinsic(extrinsic: String) :
    RpcMethod("author_submitExtrinsic", JSONArray(arrayOf(extrinsic)))

class PaymentQueryInfo(extrinsic: String) :
    RpcMethod("payment_queryInfo", JSONArray(arrayOf(extrinsic)))


