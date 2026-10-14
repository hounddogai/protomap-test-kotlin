package com.acme.transport.lastmile.proofofdelivery.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryBundleTest {
    @Test
    fun `starts in new`() {
        val item = newProofOfDeliveryBundle("code-759", "title-712")
        assertEquals(ProofOfDeliveryBundleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newProofOfDeliveryBundle("code-759", "title-712")
        try {
            item.moveTo(ProofOfDeliveryBundleStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryBundleStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newProofOfDeliveryBundle("code-759", "title-712")
        item.status = ProofOfDeliveryBundleStatus.FAILED
        assertFails("move") { item.moveTo(ProofOfDeliveryBundleStatus.NEW) }
    }
}
