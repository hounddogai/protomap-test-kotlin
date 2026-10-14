package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalProofOfDeliveryAllocationTest {
    @Test
    fun `starts in new`() {
        val item = newSeasonalProofOfDeliveryAllocation("code-6", "label-31")
        assertEquals(SeasonalProofOfDeliveryAllocationStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newSeasonalProofOfDeliveryAllocation("code-6", "label-31")
        try {
            item.moveTo(SeasonalProofOfDeliveryAllocationStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalProofOfDeliveryAllocationStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSeasonalProofOfDeliveryAllocation("code-6", "label-31")
        item.status = SeasonalProofOfDeliveryAllocationStatus.FAILED
        assertFails("move") { item.moveTo(SeasonalProofOfDeliveryAllocationStatus.NEW) }
    }
}
