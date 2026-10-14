package com.acme.transport.safety.incident.incidentmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentDraftTest {
    @Test
    fun `starts in new`() {
        val item = newIncidentDraft("code-351", "label-874")
        assertEquals(IncidentDraftStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newIncidentDraft("code-351", "label-874")
        try {
            item.moveTo(IncidentDraftStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentDraftStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newIncidentDraft("code-351", "label-874")
        item.status = IncidentDraftStatus.FAILED
        assertFails("move") { item.moveTo(IncidentDraftStatus.NEW) }
    }
}
