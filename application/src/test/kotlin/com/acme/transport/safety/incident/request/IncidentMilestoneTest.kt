package com.acme.transport.safety.incident.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentMilestoneTest {
    @Test
    fun `starts in requested`() {
        val item = newIncidentMilestone("code-287", "name-814")
        assertEquals(IncidentMilestoneStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newIncidentMilestone("code-287", "name-814")
        try {
            item.moveTo(IncidentMilestoneStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentMilestoneStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newIncidentMilestone("code-287", "name-814")
        item.status = IncidentMilestoneStatus.EXPIRED
        assertFails("move") { item.moveTo(IncidentMilestoneStatus.REQUESTED) }
    }
}
