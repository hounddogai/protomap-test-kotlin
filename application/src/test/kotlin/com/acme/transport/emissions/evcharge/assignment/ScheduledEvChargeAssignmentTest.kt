package com.acme.transport.emissions.evcharge.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledEvChargeAssignmentTest {
    @Test
    fun `starts in planned`() {
        val item = newScheduledEvChargeAssignment("code-157", "label-516")
        assertEquals(ScheduledEvChargeAssignmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newScheduledEvChargeAssignment("code-157", "label-516")
        try {
            item.moveTo(ScheduledEvChargeAssignmentStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledEvChargeAssignmentStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScheduledEvChargeAssignment("code-157", "label-516")
        item.status = ScheduledEvChargeAssignmentStatus.CANCELLED
        assertFails("move") { item.moveTo(ScheduledEvChargeAssignmentStatus.PLANNED) }
    }
}
