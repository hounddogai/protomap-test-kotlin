package com.acme.transport.network.transittime.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualTransitTimeSlotTest {
    @Test
    fun `starts in draft`() {
        val item = newManualTransitTimeSlot("code-509", "name-723")
        assertEquals(ManualTransitTimeSlotStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newManualTransitTimeSlot("code-509", "name-723")
        try {
            item.moveTo(ManualTransitTimeSlotStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualTransitTimeSlotStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualTransitTimeSlot("code-509", "name-723")
        item.status = ManualTransitTimeSlotStatus.CLOSED
        assertFails("move") { item.moveTo(ManualTransitTimeSlotStatus.DRAFT) }
    }
}
