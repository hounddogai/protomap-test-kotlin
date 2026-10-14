package com.acme.transport.safety.incident.incidentsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentSlotTest {
    @Test
    fun `starts in planned`() {
        val item = newIncidentSlot("code-637", "title-279")
        assertEquals(IncidentSlotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newIncidentSlot("code-637", "title-279")
        try {
            item.moveTo(IncidentSlotStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentSlotStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newIncidentSlot("code-637", "title-279")
        item.status = IncidentSlotStatus.CANCELLED
        assertFails("move") { item.moveTo(IncidentSlotStatus.PLANNED) }
    }
}
