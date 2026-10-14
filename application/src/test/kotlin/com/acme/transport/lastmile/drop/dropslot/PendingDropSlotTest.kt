package com.acme.transport.lastmile.drop.dropslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingDropSlotTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingDropSlot("code-33", "name-965")
        assertEquals(PendingDropSlotStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newPendingDropSlot("code-33", "name-965")
        try {
            item.moveTo(PendingDropSlotStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingDropSlotStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingDropSlot("code-33", "name-965")
        item.status = PendingDropSlotStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingDropSlotStatus.REQUESTED) }
    }
}
