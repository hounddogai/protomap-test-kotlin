package com.acme.transport.customs.hold.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldSlotTest {
    @Test
    fun `starts in planned`() {
        val item = newHoldSlot("code-997", "name-712")
        assertEquals(HoldSlotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newHoldSlot("code-997", "name-712")
        try {
            item.moveTo(HoldSlotStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldSlotStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newHoldSlot("code-997", "name-712")
        item.status = HoldSlotStatus.CANCELLED
        assertFails("move") { item.moveTo(HoldSlotStatus.PLANNED) }
    }
}
