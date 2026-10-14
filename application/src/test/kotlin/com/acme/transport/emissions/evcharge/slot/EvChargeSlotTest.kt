package com.acme.transport.emissions.evcharge.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeSlotTest {
    @Test
    fun `starts in draft`() {
        val item = newEvChargeSlot("code-15", "name-192")
        assertEquals(EvChargeSlotStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newEvChargeSlot("code-15", "name-192")
        try {
            item.moveTo(EvChargeSlotStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeSlotStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newEvChargeSlot("code-15", "name-192")
        item.status = EvChargeSlotStatus.CLOSED
        assertFails("move") { item.moveTo(EvChargeSlotStatus.DRAFT) }
    }
}
