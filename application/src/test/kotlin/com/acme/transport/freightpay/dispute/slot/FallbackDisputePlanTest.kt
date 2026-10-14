package com.acme.transport.freightpay.dispute.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDisputePlanTest {
    @Test
    fun `starts in planned`() {
        val item = newFallbackDisputePlan("code-168", "label-624")
        assertEquals(FallbackDisputePlanStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newFallbackDisputePlan("code-168", "label-624")
        try {
            item.moveTo(FallbackDisputePlanStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDisputePlanStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFallbackDisputePlan("code-168", "label-624")
        item.status = FallbackDisputePlanStatus.CANCELLED
        assertFails("move") { item.moveTo(FallbackDisputePlanStatus.PLANNED) }
    }
}
