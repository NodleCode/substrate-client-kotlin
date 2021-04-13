package io.nodle.substratesdk.types

import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters

/**
 * @author Lucien Loiseau on 09/04/21.
 */

/**
 * Represents the MultiAddress enum defined in Substrate
 *
 * See:
 *   - https://github.com/paritytech/substrate/blob/master/primitives/runtime/src/multiaddress.rs
 *   - https://github.com/paritytech/substrate/pull/7380
 */
enum class AddressType(val id: Int) {
    AccountID(0), AccountIndex(1), Raw(2), Address32(3), Address20(4)
}

abstract class MultiAddress(val type: AddressType)

// public key
class AccountIDAddress(val pubkey: ByteArray) :
    MultiAddress(AddressType.AccountID) {
    constructor(ed: Ed25519PublicKeyParameters) : this(ed.encoded)
}

// encoded string
class RawAddress(val bytes: ByteArray) : MultiAddress(AddressType.Raw)

// eth
class Address20(val bytes: ByteArray) :
    MultiAddress(AddressType.Address20) {
    init {
        require(bytes.size == 20)
    }
}

// like a 256 bits hash
class Address32(val bytes: ByteArray) :
    MultiAddress(AddressType.Address32) {
    init {
        require(bytes.size == 32)
    }
}