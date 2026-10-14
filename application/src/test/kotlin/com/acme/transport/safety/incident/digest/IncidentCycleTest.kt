package com.acme.transport.safety.incident.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentCycleTest {
    @Test
    fun `starts in planned`() {
        val item = newIncidentCycle("code-597", "label-360")
        assertEquals(IncidentCycleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newIncidentCycle("code-597", "label-360")
        try {
            item.moveTo(IncidentCycleStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentCycleStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newIncidentCycle("code-597", "label-360")
        item.status = IncidentCycleStatus.CANCELLED
        assertFails("move") { item.moveTo(IncidentCycleStatus.PLANNED) }
    }
}
