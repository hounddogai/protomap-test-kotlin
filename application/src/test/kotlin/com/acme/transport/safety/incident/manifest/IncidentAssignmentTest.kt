package com.acme.transport.safety.incident.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentAssignmentTest {
    @Test
    fun `starts in planned`() {
        val item = newIncidentAssignment("code-402", "title-205")
        assertEquals(IncidentAssignmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newIncidentAssignment("code-402", "title-205")
        try {
            item.moveTo(IncidentAssignmentStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentAssignmentStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newIncidentAssignment("code-402", "title-205")
        item.status = IncidentAssignmentStatus.CANCELLED
        assertFails("move") { item.moveTo(IncidentAssignmentStatus.PLANNED) }
    }
}
