package com.acme.transport.linehaul.capacity.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackCapacitySlotTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackCapacitySlot("code-742", "label-906")
        assertEquals(FallbackCapacitySlotStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newFallbackCapacitySlot("code-742", "label-906")
        try {
            item.moveTo(FallbackCapacitySlotStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackCapacitySlotStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackCapacitySlot("code-742", "label-906")
        item.status = FallbackCapacitySlotStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackCapacitySlotStatus.DRAFT) }
    }
}
