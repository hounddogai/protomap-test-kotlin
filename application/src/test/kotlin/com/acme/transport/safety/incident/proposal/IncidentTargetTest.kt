package com.acme.transport.safety.incident.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentTargetTest {
    @Test
    fun `starts in planned`() {
        val item = newIncidentTarget("code-53", "name-763")
        assertEquals(IncidentTargetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newIncidentTarget("code-53", "name-763")
        try {
            item.moveTo(IncidentTargetStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentTargetStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newIncidentTarget("code-53", "name-763")
        item.status = IncidentTargetStatus.CANCELLED
        assertFails("move") { item.moveTo(IncidentTargetStatus.PLANNED) }
    }
}
