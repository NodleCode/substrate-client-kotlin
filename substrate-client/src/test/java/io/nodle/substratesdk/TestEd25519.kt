package io.nodle.substratesdk

import io.nodle.substratesdk.account.Wallet
import io.nodle.substratesdk.account.signMsg
import io.nodle.substratesdk.utils.toHex
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

/**
 * @author Lucien Loiseau on 25/03/21.
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class TestEd25519 {

    @Test
    fun stage0_testSigner() {
        val wallet = Wallet("void come effort suffer camp survey warrior heavy shoot primary clutch crush open amazing screen patrol group space point ten exist slush involve unfold")
        val signature = wallet.privateKey.signMsg("Hello World!".toByteArray())
        Assert.assertThat(signature.toHex(), CoreMatchers.equalTo("4e69456ddb02d48b6aec237ca844d83ad39be9e0778ac88472c7dd036a360c31ca17acf19197427c97ce58f9ba6311b409425d4e5c785e81edb733ca9208ad02"))
    }

}