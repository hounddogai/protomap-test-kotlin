package com.acme.transport.safety.incident.incidentsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingIncidentRequestTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingIncidentRequest("code-351", "title-42")
        assertEquals(PendingIncidentRequestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newPendingIncidentRequest("code-351", "title-42")
        try {
            item.moveTo(PendingIncidentRequestStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingIncidentRequestStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingIncidentRequest("code-351", "title-42")
        item.status = PendingIncidentRequestStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingIncidentRequestStatus.PLANNED) }
    }
}
