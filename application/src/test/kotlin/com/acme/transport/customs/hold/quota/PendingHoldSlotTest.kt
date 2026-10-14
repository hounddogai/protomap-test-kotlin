package com.acme.transport.customs.hold.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingHoldSlotTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingHoldSlot("code-458", "title-800")
        assertEquals(PendingHoldSlotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newPendingHoldSlot("code-458", "title-800")
        try {
            item.moveTo(PendingHoldSlotStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingHoldSlotStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingHoldSlot("code-458", "title-800")
        item.status = PendingHoldSlotStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingHoldSlotStatus.PLANNED) }
    }
}
