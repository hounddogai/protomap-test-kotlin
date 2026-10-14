package com.acme.transport.lastmile.proofofdelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryProfileTest {
    @Test
    fun `starts in proposed`() {
        val item = newProofOfDeliveryProfile("code-656", "title-594")
        assertEquals(ProofOfDeliveryProfileStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newProofOfDeliveryProfile("code-656", "title-594")
        try {
            item.moveTo(ProofOfDeliveryProfileStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryProfileStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newProofOfDeliveryProfile("code-656", "title-594")
        item.status = ProofOfDeliveryProfileStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ProofOfDeliveryProfileStatus.PROPOSED) }
    }
}
