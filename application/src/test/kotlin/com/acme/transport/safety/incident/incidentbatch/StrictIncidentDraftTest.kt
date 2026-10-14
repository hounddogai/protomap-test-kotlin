package com.acme.transport.safety.incident.incidentbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictIncidentDraftTest {
    @Test
    fun `starts in open`() {
        val item = newStrictIncidentDraft("code-437", "name-980")
        assertEquals(StrictIncidentDraftStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newStrictIncidentDraft("code-437", "name-980")
        try {
            item.moveTo(StrictIncidentDraftStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictIncidentDraftStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictIncidentDraft("code-437", "name-980")
        item.status = StrictIncidentDraftStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictIncidentDraftStatus.OPEN) }
    }
}
