package com.acme.transport.safety.incident.incidentplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentRollupTest {
    @Test
    fun `starts in planned`() {
        val item = newIncidentRollup("code-470", "label-896")
        assertEquals(IncidentRollupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newIncidentRollup("code-470", "label-896")
        try {
            item.moveTo(IncidentRollupStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentRollupStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newIncidentRollup("code-470", "label-896")
        item.status = IncidentRollupStatus.CANCELLED
        assertFails("move") { item.moveTo(IncidentRollupStatus.PLANNED) }
    }
}
