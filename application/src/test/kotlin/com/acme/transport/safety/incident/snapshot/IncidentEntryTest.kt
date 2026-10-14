package com.acme.transport.safety.incident.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentEntryTest {
    @Test
    fun `starts in proposed`() {
        val item = newIncidentEntry("code-193", "label-941")
        assertEquals(IncidentEntryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newIncidentEntry("code-193", "label-941")
        try {
            item.moveTo(IncidentEntryStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentEntryStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newIncidentEntry("code-193", "label-941")
        item.status = IncidentEntryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(IncidentEntryStatus.PROPOSED) }
    }
}
