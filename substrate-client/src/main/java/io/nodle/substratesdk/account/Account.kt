package io.nodle.substratesdk.account

import io.nodle.substratesdk.types.AccountIDAddress
import io.nodle.substratesdk.types.MultiAddress
import io.nodle.substratesdk.utils.hexToBa
import io.nodle.substratesdk.utils.toHex
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import java.lang.Exception

class InvalidAccount : Exception("invalid account")

/**
 * @author Lucien Loiseau on 29/07/20.
 */
open class Account() {
    lateinit var ss58: String

    constructor(pub: Ed25519PublicKeyParameters) : this() {
        ss58 = pub.encoded.toSS58()
    }

    @Throws(InvalidAccount::class)
    constructor(accountId: String) : this() {
        val accountBa = if (accountId.startsWith("0x")) {
            accountId.hexToBa()
        } else { // we assume it is ss58
            try {
                accountId.ss58ToBa()
            } catch (e: Exception) {
                throw InvalidAccount()
            }
        }

        if (accountBa.size != 32) {
            throw InvalidAccount()
        }
        ss58 = accountBa.toSS58()
    }

    @Throws(InvalidAccount::class)
    constructor(accountBa: ByteArray) : this() {
        if (accountBa.size != 32) {
            throw InvalidAccount()
        }
        ss58 = accountBa.toSS58()
    }

    fun toMultiAddress(): MultiAddress {
        return AccountIDAddress(ss58.ss58ToBa())
    }

    fun toSS58(): String {
        return ss58
    }

    fun toSS58(substrateID: Byte): String {
        return ss58.ss58ToBa().toSS58(substrateID)
    }

    fun toHex(): String {
        return "0x" + ss58.ss58ToBa().toHex()
    }

    fun toU8a(): ByteArray {
        return ss58.ss58ToBa()
    }
}

class Wallet : Account {

    var privateKey: Ed25519PrivateKeyParameters

    constructor(privateKeyParameters: Ed25519PrivateKeyParameters) {
        this.privateKey = privateKeyParameters
        this.ss58 = privateKey.generatePublicKey().encoded.toSS58()
    }

    @Throws(InvalidAccount::class)
    constructor(mnemonic: String) {
        try {
            this.privateKey = Mnemonic.generateEd25519FromMnemonic(mnemonic.split(" "), "")
            this.ss58 = privateKey.generatePublicKey().encoded.toSS58()
        } catch (e: Exception) {
            throw InvalidAccount()
        }
    }
}