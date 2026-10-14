package com.acme.transport.safety.incident.incidentsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentRequestTest {
    @Test
    fun `starts in draft`() {
        val item = newIncidentRequest("code-654", "title-81")
        assertEquals(IncidentRequestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newIncidentRequest("code-654", "title-81")
        try {
            item.moveTo(IncidentRequestStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentRequestStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newIncidentRequest("code-654", "title-81")
        item.status = IncidentRequestStatus.CLOSED
        assertFails("move") { item.moveTo(IncidentRequestStatus.DRAFT) }
    }
}
