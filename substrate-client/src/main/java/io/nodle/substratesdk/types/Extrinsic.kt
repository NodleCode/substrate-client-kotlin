package io.nodle.substratesdk.types

import java.math.BigInteger

/**
 * @author Lucien Loiseau on 26/06/20.
 */
data class Extrinsic(
    val signature: ExtrinsicSignature,
    val method: ExtrinsicCall
)

data class ExtrinsicSignature(
    val signer: MultiAddress,
    val signature: ExtrinsicEd25519Signature,
    val era: ExtrinsicEra,
    val nonce: Long,
    val tip: BigInteger
)

data class ExtrinsicEd25519Signature(val signature: ByteArray)

abstract class ExtrinsicEra

data class ImmortalEra(val value: Long = 0x00) : ExtrinsicEra()

data class MortalEra(val period: Long, val phase: Long) : ExtrinsicEra()

data class ExtrinsicPayload(
    val blockHash: ByteArray,
    val era: ExtrinsicEra,
    val genesisHash: ByteArray,
    val method: ExtrinsicCall,
    val nonce: Long,
    val specVersion: Long,
    val tip: BigInteger,
    val transactionVersion: Long
)