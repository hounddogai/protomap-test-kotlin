package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryScoreTest {
    @Test
    fun `starts in proposed`() {
        val item = newProofOfDeliveryScore("code-827", "title-804")
        assertEquals(ProofOfDeliveryScoreStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newProofOfDeliveryScore("code-827", "title-804")
        try {
            item.moveTo(ProofOfDeliveryScoreStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryScoreStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newProofOfDeliveryScore("code-827", "title-804")
        item.status = ProofOfDeliveryScoreStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ProofOfDeliveryScoreStatus.PROPOSED) }
    }
}
