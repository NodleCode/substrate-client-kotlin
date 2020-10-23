package io.nodle.substratesdk.account

import io.github.novacrypto.bip39.MnemonicGenerator
import io.github.novacrypto.bip39.MnemonicValidator
import io.github.novacrypto.bip39.Validation.InvalidChecksumException
import io.github.novacrypto.bip39.Validation.InvalidWordCountException
import io.github.novacrypto.bip39.Validation.UnexpectedWhiteSpaceException
import io.github.novacrypto.bip39.Validation.WordNotFoundException
import io.github.novacrypto.bip39.Words
import io.github.novacrypto.bip39.wordlists.English
import org.bouncycastle.crypto.digests.SHA512Digest
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.KeyParameter
import java.security.SecureRandom
import java.util.*

/**
 * @author Lucien Loiseau on 28/05/20.
 */
object Mnemonic {
    
    fun newTwelveWords(): List<String> {
        /*
        val ret = mutableListOf<String>()
        val entropy = ByteArray(Words.TWELVE.byteLength())
        SecureRandom().nextBytes(entropy)
        MnemonicGenerator(English.INSTANCE)
            .createMnemonic(entropy) {
                    s: CharSequence? -> ret.add(s.toString())
            }
        return ret
        */
        val sb = StringBuilder()
        val entropy = ByteArray(Words.TWELVE.byteLength())
        SecureRandom().nextBytes(entropy)
        MnemonicGenerator(English.INSTANCE)
            .createMnemonic(
                entropy
            ) { s: CharSequence? -> sb.append(s) }
        return sb.split(" ")
    }

    fun seedFromMnemonic(mnemonic: List<String>): ByteArray {
        val entropyWithChecksum = MnemonicValidator
            .ofWordList(English.INSTANCE)
            .entropyWithChecksum(mnemonic)
        val entropy = Arrays.copyOf(entropyWithChecksum, entropyWithChecksum.size - 1);
        return entropy
    }

    @Throws(
        InvalidChecksumException::class,
        InvalidWordCountException::class,
        WordNotFoundException::class,
        UnexpectedWhiteSpaceException::class)
    fun generateEd25519FromMnemonic(mnemonic: List<String>, password: String): Ed25519PrivateKeyParameters {
        MnemonicValidator
            .ofWordList(English.INSTANCE)
            .validate(mnemonic)

        // get entropy from mnemonic phrase
        val entropyWithChecksum = MnemonicValidator
            .ofWordList(English.INSTANCE)
            .entropyWithChecksum(mnemonic)
        val entropy = Arrays.copyOf(entropyWithChecksum, entropyWithChecksum.size - 1);

        // generate the seed
        val salt = "mnemonic$password"
        val gen = PKCS5S2ParametersGenerator(SHA512Digest())
        gen.init(entropy, salt.toByteArray(), 2048)
        val encoded = (gen.generateDerivedParameters(512) as KeyParameter).key

        // generate private key
        return Ed25519PrivateKeyParameters(encoded, 0)
    }
}