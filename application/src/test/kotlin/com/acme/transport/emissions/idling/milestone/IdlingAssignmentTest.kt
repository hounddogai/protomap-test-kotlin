package com.acme.transport.emissions.idling.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingAssignmentTest {
    @Test
    fun `starts in planned`() {
        val item = newIdlingAssignment("code-401", "label-842")
        assertEquals(IdlingAssignmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newIdlingAssignment("code-401", "label-842")
        try {
            item.moveTo(IdlingAssignmentStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingAssignmentStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newIdlingAssignment("code-401", "label-842")
        item.status = IdlingAssignmentStatus.CANCELLED
        assertFails("move") { item.moveTo(IdlingAssignmentStatus.PLANNED) }
    }
}
