package io.nodle.substratesdk

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.nodle.substratesdk.account.Wallet
import io.nodle.substratesdk.rpc.SubstrateProvider
import io.reactivex.rxjava3.kotlin.subscribeBy
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockserver.client.server.MockServerClient

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
    fun stage0_testMetadata(rpcUrl: String, aliceMnemonic: String, bobMnemonic: String, carlaMnemonic: String) {
        val provider = SubstrateProvider(rpcUrl)

        val meta = provider.getMetadata().blockingGet()
        Assert.assertThat(
            meta,
            CoreMatchers.notNullValue()
        )
    }

    @Test
    @Parameters(method = "testChainConfig")
    fun stage0_testGenesisHash(rpcUrl: String, aliceMnemonic: String, bobMnemonic: String, carlaMnemonic: String) {
        val provider = SubstrateProvider(rpcUrl)
        val hash = provider.getGenesisHash().blockingGet()
        Assert.assertThat(
            hash,
            CoreMatchers.notNullValue()
        )
    }

    @Test
    @Parameters(method = "testChainConfig")
    fun stage0_testSpecVersion(rpcUrl: String, aliceMnemonic: String, bobMnemonic: String, carlaMnemonic: String) {
        val provider = SubstrateProvider(rpcUrl)
        val specVersion = provider.getSpecVersion().blockingGet()
        Assert.assertThat(
            specVersion,
            CoreMatchers.notNullValue()
        )
    }

    @Test
    @Parameters(method = "testChainConfig")
    fun stage0_testTransactionVersion(rpcUrl: String, aliceMnemonic: String, bobMnemonic: String, carlaMnemonic: String) {
        val provider = SubstrateProvider(rpcUrl)
        val specVersion = provider.getTransactionVersion().blockingGet()
        Assert.assertThat(
            specVersion,
            CoreMatchers.notNullValue()
        )
    }

    @Test
    @Parameters(method = "testChainConfig")
    fun stage1_testBalance(rpcUrl: String, aliceMnemonic: String, bobMnemonic: String, carlaMnemonic: String) {
        val provider = SubstrateProvider(rpcUrl)

        val wallet1 =
            Wallet(carlaMnemonic)
        val balance1 = wallet1.getAccountInfo(provider).blockingGet()
        Assert.assertThat(balance1.data.free.toLong(), CoreMatchers.equalTo(1000000000000))

        runBlocking { delay(12000) }

        val wallet2 =
            Wallet(aliceMnemonic)
        val balance2 = wallet2.getAccountInfo(provider).blockingGet()
        Assert.assertThat(balance2.data.free.toLong(), CoreMatchers.notNullValue())

        val wallet3 =
            Wallet(bobMnemonic)
        val balance3 = wallet3.getAccountInfo(provider).blockingGet()
        Assert.assertThat(balance3.data.free.toLong(), CoreMatchers.notNullValue())

        val wallet4 =
            Wallet("cherry royal innocent naive motor album pride humble deliver leaf trick series")
        val balance4 = wallet4.getAccountInfo(provider).blockingGet()
        Assert.assertThat(balance4.data.free.toLong(), CoreMatchers.equalTo(0))
    }


    @ExperimentalUnsignedTypes
    @Test
    @Parameters(method = "testChainConfig")
    fun stage2_testSignTx(rpcUrl: String, aliceMnemonic: String, bobMnemonic: String, carlaMnemonic: String) {
        val provider = SubstrateProvider(rpcUrl)
        val src = Wallet(aliceMnemonic)
        val destWallet = Wallet(bobMnemonic)
        val txhash = src.signTx(provider, destWallet, 10.toBigInteger()).blockingGet()
        Assert.assertThat(txhash, CoreMatchers.notNullValue())
    }

    @ExperimentalUnsignedTypes
    @Test
    @Parameters(method = "testChainConfig")
    fun stage3_testTransfer(rpcUrl: String, aliceMnemonic: String, bobMnemonic: String, carlaMnemonic: String) {
        val provider = SubstrateProvider(rpcUrl)

        val wallet1 = Wallet(aliceMnemonic)
        val balance1 = wallet1.getAccountInfo(provider).blockingGet().data.free.toLong()
        Assert.assertThat(balance1, CoreMatchers.notNullValue())

        val wallet2 = Wallet(bobMnemonic)
        val balance2 = wallet2.getAccountInfo(provider).blockingGet().data.free.toLong()
        Assert.assertThat(balance2, CoreMatchers.notNullValue())

        val balancesrc : Long
        val walletsrc = if (balance1 < balance2) {
            balancesrc = balance2
            wallet2
        } else {
            balancesrc = balance1
            wallet1
        }

        val balancedst : Long
        val walletdst = if (balance1 < balance2) {
            balancedst = balance1
            wallet1
        } else {
            balancedst = balance2
            wallet2
        }

        // estimate fee
        val tx1 = walletsrc.signTx(provider, walletdst, 1000000.toBigInteger()).blockingGet()
        val fee1 = tx1.estimateFee(provider).blockingGet()

        // send fund
        val txhash1 = walletsrc.signAndSend(provider, walletdst, 1000000.toBigInteger()).blockingGet()
        Assert.assertThat(txhash1, CoreMatchers.notNullValue())

        // wait for transaction to be validated
        runBlocking {
            delay(15000)
        }

        // check that balance as changed and amount is coherent
        val balanceSrcAfter = walletsrc.getAccountInfo(provider).blockingGet().data.free.toLong()
        Assert.assertThat(balanceSrcAfter < balancesrc,  CoreMatchers.equalTo(true))
        val expected = (fee1.toLong() + 1000000)
        val actual = (balancesrc - balanceSrcAfter)
        val diff = expected - actual
        val ratio = diff*100/expected

        // we shouldn't expect more than a 10% slip from expected price
        Assert.assertThat(ratio < 10, CoreMatchers.equalTo(true))


        val balanceDstAfter = walletdst.getAccountInfo(provider).blockingGet().data.free.toLong()
        Assert.assertThat(balanceDstAfter > balancedst, CoreMatchers.equalTo(true))

        // destination should receive exactly what's expected
        Assert.assertThat(balanceDstAfter - balancedst, CoreMatchers.equalTo(1000000))

        /* todo: mortal era does not work!
        val txhash2 = walletsrc.signAndSend(provider, walletdst, 10.toBigInteger(), MortalEra(64,38)).blockingGet()
        Assert.assertThat(txhash2, CoreMatchers.notNullValue())
        */
    }

    @ExperimentalUnsignedTypes
    @Test
    @Parameters(method = "testChainConfig")
    fun stage4_testEstimateFee(rpcUrl: String, aliceMnemonic: String, bobMnemonic: String, carlaMnemonic: String) {
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
        val fee4 = tx4.estimateFee(provider).blockingGet()
        Assert.assertThat(fee4, CoreMatchers.not(0.toBigInteger()))
    }

    @Test
    fun stage5_testUrlCannotConnect() {
        val rpcUrl = "wss://ThisSubstrateUrlDoesNotExist.com"
        val provider = SubstrateProvider(rpcUrl)

        provider.getMetadata()
            .subscribeBy(
                onError = {
                },
                onSuccess = {
                    Assert.fail()
                })

        var mockServer: MockServerClient = MockServerClient("localhost", 5568)
        val url = "http://localhost:5568"
    }

    @Test
    fun stage5_testUrlCannotWebSocket() {
        val rpcUrl = "ws://google.com/"
        val provider = SubstrateProvider(rpcUrl)

        provider.getMetadata()
            .subscribeBy(
                onError = {
                },
                onSuccess = {
                    Assert.fail()
                })
    }
}