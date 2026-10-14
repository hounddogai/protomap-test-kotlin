package com.acme.transport.crewing.availability.scheduledavailabilitytier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedAvailabilitySlotTest {
    @Test
    fun `starts in idle`() {
        val item = newCappedAvailabilitySlot("code-802", "title-724")
        assertEquals(CappedAvailabilitySlotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newCappedAvailabilitySlot("code-802", "title-724")
        try {
            item.moveTo(CappedAvailabilitySlotStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedAvailabilitySlotStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCappedAvailabilitySlot("code-802", "title-724")
        item.status = CappedAvailabilitySlotStatus.STOPPED
        assertFails("move") { item.moveTo(CappedAvailabilitySlotStatus.IDLE) }
    }
}
