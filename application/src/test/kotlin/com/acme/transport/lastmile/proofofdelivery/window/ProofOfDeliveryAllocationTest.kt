package com.acme.transport.lastmile.proofofdelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryAllocationTest {
    @Test
    fun `starts in proposed`() {
        val item = newProofOfDeliveryAllocation("code-865", "label-532")
        assertEquals(ProofOfDeliveryAllocationStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newProofOfDeliveryAllocation("code-865", "label-532")
        try {
            item.moveTo(ProofOfDeliveryAllocationStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryAllocationStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newProofOfDeliveryAllocation("code-865", "label-532")
        item.status = ProofOfDeliveryAllocationStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ProofOfDeliveryAllocationStatus.PROPOSED) }
    }
}
