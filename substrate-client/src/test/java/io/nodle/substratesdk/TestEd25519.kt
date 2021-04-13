package io.nodle.substratesdk

import io.nodle.substratesdk.account.Wallet
import io.nodle.substratesdk.account.signMsg
import io.nodle.substratesdk.utils.hexToBa
import io.nodle.substratesdk.utils.toHex
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
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

    @Test
    fun stage1_testSigner() {
        val wallet = Wallet(Ed25519PrivateKeyParameters("e0dab57d0f865f5add6121cc0d4351ac5eae7f66d7f371bf39d30b7ae913d2c4".hexToBa(), 0))
        val signature = wallet.privateKey.signMsg("0300ffe432904cb94fdf27c6fdaad7985391f7bbe578e2df5ae1a6788bb07b5d6f15cf280048002d00000003000000f18113a0061d72db857d3b3e2f261e1c214989d191fa57380e633cc5c1a2ad38f18113a0061d72db857d3b3e2f261e1c214989d191fa57380e633cc5c1a2ad38".hexToBa())
        Assert.assertThat(signature.toHex(), CoreMatchers.equalTo("2f0fada4259e8faf06806d0d56e72a0fb592f8923407f46637f4edd3e13b80082e75b22615059707d9523baf7ed706249c89ef518c0ff06912b654101385990f"))
    }
}