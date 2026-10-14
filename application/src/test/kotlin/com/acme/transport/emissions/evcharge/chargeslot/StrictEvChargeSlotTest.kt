package com.acme.transport.emissions.evcharge.chargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictEvChargeSlotTest {
    @Test
    fun `starts in planned`() {
        val item = newStrictEvChargeSlot("code-336", "label-901")
        assertEquals(StrictEvChargeSlotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newStrictEvChargeSlot("code-336", "label-901")
        try {
            item.moveTo(StrictEvChargeSlotStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictEvChargeSlotStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newStrictEvChargeSlot("code-336", "label-901")
        item.status = StrictEvChargeSlotStatus.CANCELLED
        assertFails("move") { item.moveTo(StrictEvChargeSlotStatus.PLANNED) }
    }
}
