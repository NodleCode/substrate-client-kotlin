package io.nodle.substratesdk

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.nodle.substratesdk.account.Wallet
import io.nodle.substratesdk.rpc.SubstrateProvider
import io.nodle.substratesdk.scale.toU8a
import io.nodle.substratesdk.utils.toHex
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * @author Lucien Loiseau on 31/05/20.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(JUnitParamsRunner::class)
class TestRpc {

    private fun getResourceAsText(path: String): String? {
        return this.javaClass.classLoader.getResource(path)?.readText()
    }

    fun testChainConfig(): Array<Any>? {
        val config = getResourceAsText("rpc-test.config")
        return config
            ?.let {
                csvReader().readAll(config)
                    .map {
                        it.toTypedArray()
                    }.toTypedArray()
            } ?: arrayOf()
    }

    @ExperimentalUnsignedTypes
    @Test
    @Parameters(method = "testChainConfig")
    fun stage0_testMetadata(rpcUrl: String, aliceMnemonic: String, bobMnemonic: String) {
        val provider = SubstrateProvider(rpcUrl)

        val meta = provider.getMetadata().blockingGet()
        Assert.assertThat(
            meta,
            CoreMatchers.notNullValue()
        )
    }

    @Test
    @Parameters(method = "testChainConfig")
    fun stage0_testGenesisHash(rpcUrl: String, aliceMnemonic: String, bobMnemonic: String) {
        val provider = SubstrateProvider(rpcUrl)
        val hash = provider.getGenesisHash().blockingGet()
        Assert.assertThat(
            hash,
            CoreMatchers.notNullValue()
        )
    }

    @Test
    @Parameters(method = "testChainConfig")
    fun stage0_testSpecVersion(rpcUrl: String, aliceMnemonic: String, bobMnemonic: String) {
        val provider = SubstrateProvider(rpcUrl)
        val specVersion = provider.getSpecVersion().blockingGet()
        Assert.assertThat(
            specVersion,
            CoreMatchers.notNullValue()
        )
    }

    @Test
    @Parameters(method = "testChainConfig")
    fun stage0_testTransactionVersion(rpcUrl: String, aliceMnemonic: String, bobMnemonic: String) {
        val provider = SubstrateProvider(rpcUrl)
        val specVersion = provider.getTransactionVersion().blockingGet()
        Assert.assertThat(
            specVersion,
            CoreMatchers.notNullValue()
        )
    }

    @Test
    @Parameters(method = "testChainConfig")
    fun stage1_testBalance(rpcUrl: String, aliceMnemonic: String, bobMnemonic: String) {
        val provider = SubstrateProvider(rpcUrl)

        val wallet2 =
            Wallet(aliceMnemonic)
        val balance2 = wallet2.getAccountInfo(provider).blockingGet()
        Assert.assertThat(balance2.data.free.toLong(), CoreMatchers.notNullValue())

        val wallet3 =
            Wallet(bobMnemonic)
        val balance3 = wallet3.getAccountInfo(provider).blockingGet()
        Assert.assertThat(balance3.data.free.toLong(), CoreMatchers.notNullValue())
    }

    @ExperimentalUnsignedTypes
    @Test
    @Parameters(method = "testChainConfig")
    fun stage2_testSignTx(rpcUrl: String, aliceMnemonic: String, bobMnemonic: String) {
        val provider = SubstrateProvider(rpcUrl)
        val src =
            Wallet(aliceMnemonic)
        val destWallet =
            Wallet(bobMnemonic)
        val txhash = src.signTx(provider, destWallet, 10.toBigInteger()).blockingGet()
        Assert.assertThat(txhash, CoreMatchers.notNullValue())
    }

    @ExperimentalUnsignedTypes
    @Test
    @Parameters(method = "testChainConfig")
    fun stage3_testTransfer(rpcUrl: String, aliceMnemonic: String, bobMnemonic: String) {
        val provider = SubstrateProvider(rpcUrl)

        val wallet1 =
            Wallet(aliceMnemonic)
        val balance1 = wallet1.getAccountInfo(provider).blockingGet().data.free.toLong()
        Assert.assertThat(balance1, CoreMatchers.notNullValue())

        val wallet2 =
            Wallet(bobMnemonic)
        val balance2 = wallet2.getAccountInfo(provider).blockingGet().data.free.toLong()
        Assert.assertThat(balance2, CoreMatchers.notNullValue())

        val walletsrc = if (balance1 < balance2) {
            wallet2
        } else {
            wallet1
        }
        val walletdst = if (balance1 < balance2) {
            wallet1
        } else {
            wallet2
        }

        val txhash1 = walletsrc.signAndSend(provider, walletdst, 10.toBigInteger()).blockingGet()
        Assert.assertThat(txhash1, CoreMatchers.notNullValue())

        /* todo: mortal era does not work!
        val txhash2 = walletsrc.signAndSend(provider, walletdst, 10.toBigInteger(), MortalEra(64,38)).blockingGet()
        Assert.assertThat(txhash2, CoreMatchers.notNullValue())
        */
    }

    @ExperimentalUnsignedTypes
    @Test
    @Parameters(method = "testChainConfig")
    fun stage4_testEstimateFee(rpcUrl: String, aliceMnemonic: String, bobMnemonic: String) {
        val provider = SubstrateProvider(rpcUrl)
        val src = Wallet(aliceMnemonic)
        val destWallet = Wallet(bobMnemonic)

        val tx1 = src.signTx(provider, destWallet, 1000000.toBigInteger()).blockingGet()
        val fee1 = tx1.estimateFee(provider).blockingGet()
        Assert.assertThat(fee1, CoreMatchers.not(0.toBigInteger()))

        val tx2 = src.signTx(provider, destWallet, 100000000.toBigInteger()).blockingGet()
        val fee2 = tx2.estimateFee(provider).blockingGet()
        Assert.assertThat(fee2, CoreMatchers.not(0.toBigInteger()))

        val tx3 = src.signTx(provider, destWallet, 1000000000.toBigInteger()).blockingGet()
        val fee3 = tx3.estimateFee(provider).blockingGet()
        Assert.assertThat(fee3, CoreMatchers.not(0.toBigInteger()))

        val tx4 = src.signTx(provider, destWallet, 1000000000000.toBigInteger()).blockingGet()
        val tx = tx4.toU8a().toHex()
        val fee4 = tx4.estimateFee(provider).blockingGet()
        Assert.assertThat(fee4, CoreMatchers.not(0.toBigInteger()))
    }

}