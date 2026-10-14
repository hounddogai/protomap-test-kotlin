package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedProofOfDeliveryAllocationTest {
    @Test
    fun `starts in open`() {
        val item = newCappedProofOfDeliveryAllocation("code-465", "title-850")
        assertEquals(CappedProofOfDeliveryAllocationStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newCappedProofOfDeliveryAllocation("code-465", "title-850")
        try {
            item.moveTo(CappedProofOfDeliveryAllocationStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedProofOfDeliveryAllocationStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCappedProofOfDeliveryAllocation("code-465", "title-850")
        item.status = CappedProofOfDeliveryAllocationStatus.ARCHIVED
        assertFails("move") { item.moveTo(CappedProofOfDeliveryAllocationStatus.OPEN) }
    }
}
