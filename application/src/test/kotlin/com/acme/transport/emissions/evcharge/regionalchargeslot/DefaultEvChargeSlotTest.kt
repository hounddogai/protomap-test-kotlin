package com.acme.transport.emissions.evcharge.regionalchargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultEvChargeSlotTest {
    @Test
    fun `starts in planned`() {
        val item = newDefaultEvChargeSlot("code-567", "title-7")
        assertEquals(DefaultEvChargeSlotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newDefaultEvChargeSlot("code-567", "title-7")
        try {
            item.moveTo(DefaultEvChargeSlotStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultEvChargeSlotStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDefaultEvChargeSlot("code-567", "title-7")
        item.status = DefaultEvChargeSlotStatus.CANCELLED
        assertFails("move") { item.moveTo(DefaultEvChargeSlotStatus.PLANNED) }
    }
}
