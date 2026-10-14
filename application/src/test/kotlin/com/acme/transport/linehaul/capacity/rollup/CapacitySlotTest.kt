package com.acme.transport.linehaul.capacity.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacitySlotTest {
    @Test
    fun `starts in proposed`() {
        val item = newCapacitySlot("code-880", "label-975")
        assertEquals(CapacitySlotStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newCapacitySlot("code-880", "label-975")
        try {
            item.moveTo(CapacitySlotStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacitySlotStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCapacitySlot("code-880", "label-975")
        item.status = CapacitySlotStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CapacitySlotStatus.PROPOSED) }
    }
}
