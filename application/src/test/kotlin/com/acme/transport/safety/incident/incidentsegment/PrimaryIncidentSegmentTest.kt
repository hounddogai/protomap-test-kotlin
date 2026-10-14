package com.acme.transport.safety.incident.incidentsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryIncidentSegmentTest {
    @Test
    fun `starts in planned`() {
        val item = newPrimaryIncidentSegment("code-824", "title-316")
        assertEquals(PrimaryIncidentSegmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newPrimaryIncidentSegment("code-824", "title-316")
        try {
            item.moveTo(PrimaryIncidentSegmentStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryIncidentSegmentStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPrimaryIncidentSegment("code-824", "title-316")
        item.status = PrimaryIncidentSegmentStatus.CANCELLED
        assertFails("move") { item.moveTo(PrimaryIncidentSegmentStatus.PLANNED) }
    }
}
