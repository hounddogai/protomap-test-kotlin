package com.acme.transport.freightpay.dispute.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredDisputeSlotTest {
    @Test
    fun `starts in created`() {
        val item = newTieredDisputeSlot("code-155", "label-308")
        assertEquals(TieredDisputeSlotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTieredDisputeSlot("code-155", "label-308")
        try {
            item.moveTo(TieredDisputeSlotStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredDisputeSlotStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTieredDisputeSlot("code-155", "label-308")
        item.status = TieredDisputeSlotStatus.RETIRED
        assertFails("move") { item.moveTo(TieredDisputeSlotStatus.CREATED) }
    }
}
