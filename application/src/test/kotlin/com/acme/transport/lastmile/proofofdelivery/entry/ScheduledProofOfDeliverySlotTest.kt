package com.acme.transport.lastmile.proofofdelivery.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledProofOfDeliverySlotTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledProofOfDeliverySlot("code-176", "name-811")
        assertEquals(ScheduledProofOfDeliverySlotStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newScheduledProofOfDeliverySlot("code-176", "name-811")
        try {
            item.moveTo(ScheduledProofOfDeliverySlotStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledProofOfDeliverySlotStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledProofOfDeliverySlot("code-176", "name-811")
        item.status = ScheduledProofOfDeliverySlotStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledProofOfDeliverySlotStatus.DRAFT) }
    }
}
