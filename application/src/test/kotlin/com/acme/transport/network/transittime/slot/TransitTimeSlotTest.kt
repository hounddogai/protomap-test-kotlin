package com.acme.transport.network.transittime.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeSlotTest {
    @Test
    fun `starts in open`() {
        val item = newTransitTimeSlot("code-371", "title-332")
        assertEquals(TransitTimeSlotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newTransitTimeSlot("code-371", "title-332")
        try {
            item.moveTo(TransitTimeSlotStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeSlotStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTransitTimeSlot("code-371", "title-332")
        item.status = TransitTimeSlotStatus.ARCHIVED
        assertFails("move") { item.moveTo(TransitTimeSlotStatus.OPEN) }
    }
}
