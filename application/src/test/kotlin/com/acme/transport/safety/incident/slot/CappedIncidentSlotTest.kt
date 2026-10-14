package com.acme.transport.safety.incident.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedIncidentSlotTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedIncidentSlot("code-721", "title-290")
        assertEquals(CappedIncidentSlotStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newCappedIncidentSlot("code-721", "title-290")
        try {
            item.moveTo(CappedIncidentSlotStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedIncidentSlotStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedIncidentSlot("code-721", "title-290")
        item.status = CappedIncidentSlotStatus.CLOSED
        assertFails("move") { item.moveTo(CappedIncidentSlotStatus.DRAFT) }
    }
}
