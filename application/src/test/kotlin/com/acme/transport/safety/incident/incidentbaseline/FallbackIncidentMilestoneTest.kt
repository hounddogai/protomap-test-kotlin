package com.acme.transport.safety.incident.incidentbaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackIncidentMilestoneTest {
    @Test
    fun `starts in requested`() {
        val item = newFallbackIncidentMilestone("code-148", "title-707")
        assertEquals(FallbackIncidentMilestoneStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newFallbackIncidentMilestone("code-148", "title-707")
        try {
            item.moveTo(FallbackIncidentMilestoneStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackIncidentMilestoneStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFallbackIncidentMilestone("code-148", "title-707")
        item.status = FallbackIncidentMilestoneStatus.EXPIRED
        assertFails("move") { item.moveTo(FallbackIncidentMilestoneStatus.REQUESTED) }
    }
}
