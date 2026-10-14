package com.acme.transport.freightpay.dispute.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeAdjustmentTest {
    @Test
    fun `starts in open`() {
        val item = newDisputeAdjustment("code-856", "label-576")
        assertEquals(DisputeAdjustmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newDisputeAdjustment("code-856", "label-576")
        try {
            item.moveTo(DisputeAdjustmentStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeAdjustmentStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDisputeAdjustment("code-856", "label-576")
        item.status = DisputeAdjustmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(DisputeAdjustmentStatus.OPEN) }
    }
}
