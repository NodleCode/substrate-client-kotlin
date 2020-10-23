package io.nodle.substratesdk.account

import org.bouncycastle.crypto.Signer
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer

/**
 * @author Lucien Loiseau on 30/06/20.
 */
fun Ed25519PrivateKeyParameters.signMsg(msg: ByteArray) : ByteArray {
    val signer: Signer = Ed25519Signer()
    signer.init(true, this)
    signer.update(msg, 0, msg.size)
    return signer.generateSignature()
}

