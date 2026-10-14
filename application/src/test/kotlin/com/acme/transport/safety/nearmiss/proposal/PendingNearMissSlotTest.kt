package com.acme.transport.safety.nearmiss.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingNearMissSlotTest {
    @Test
    fun `starts in draft`() {
        val item = newPendingNearMissSlot("code-635", "name-609")
        assertEquals(PendingNearMissSlotStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newPendingNearMissSlot("code-635", "name-609")
        try {
            item.moveTo(PendingNearMissSlotStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingNearMissSlotStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPendingNearMissSlot("code-635", "name-609")
        item.status = PendingNearMissSlotStatus.CLOSED
        assertFails("move") { item.moveTo(PendingNearMissSlotStatus.DRAFT) }
    }
}
