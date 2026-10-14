package com.acme.transport.freightpay.dispute.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDisputePlanTest {
    @Test
    fun `starts in planned`() {
        val item = newRegionalDisputePlan("code-362", "label-584")
        assertEquals(RegionalDisputePlanStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newRegionalDisputePlan("code-362", "label-584")
        try {
            item.moveTo(RegionalDisputePlanStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDisputePlanStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRegionalDisputePlan("code-362", "label-584")
        item.status = RegionalDisputePlanStatus.CANCELLED
        assertFails("move") { item.moveTo(RegionalDisputePlanStatus.PLANNED) }
    }
}
