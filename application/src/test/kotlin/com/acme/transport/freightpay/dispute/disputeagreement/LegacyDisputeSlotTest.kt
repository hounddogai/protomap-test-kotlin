package com.acme.transport.freightpay.dispute.disputeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyDisputeSlotTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyDisputeSlot("code-914", "title-896")
        assertEquals(LegacyDisputeSlotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newLegacyDisputeSlot("code-914", "title-896")
        try {
            item.moveTo(LegacyDisputeSlotStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyDisputeSlotStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyDisputeSlot("code-914", "title-896")
        item.status = LegacyDisputeSlotStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyDisputeSlotStatus.IDLE) }
    }
}
