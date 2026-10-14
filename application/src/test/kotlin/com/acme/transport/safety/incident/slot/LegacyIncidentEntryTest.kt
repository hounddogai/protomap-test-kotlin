package com.acme.transport.safety.incident.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyIncidentEntryTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyIncidentEntry("code-850", "label-51")
        assertEquals(LegacyIncidentEntryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newLegacyIncidentEntry("code-850", "label-51")
        try {
            item.moveTo(LegacyIncidentEntryStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyIncidentEntryStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyIncidentEntry("code-850", "label-51")
        item.status = LegacyIncidentEntryStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyIncidentEntryStatus.PLANNED) }
    }
}
