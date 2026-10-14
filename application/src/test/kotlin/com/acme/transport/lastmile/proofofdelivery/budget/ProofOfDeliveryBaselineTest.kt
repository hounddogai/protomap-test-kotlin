package com.acme.transport.lastmile.proofofdelivery.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryBaselineTest {
    @Test
    fun `starts in draft`() {
        val item = newProofOfDeliveryBaseline("code-926", "label-78")
        assertEquals(ProofOfDeliveryBaselineStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newProofOfDeliveryBaseline("code-926", "label-78")
        try {
            item.moveTo(ProofOfDeliveryBaselineStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryBaselineStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newProofOfDeliveryBaseline("code-926", "label-78")
        item.status = ProofOfDeliveryBaselineStatus.CLOSED
        assertFails("move") { item.moveTo(ProofOfDeliveryBaselineStatus.DRAFT) }
    }
}
