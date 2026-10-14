package com.acme.transport.fleetops.serviceplan.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictServicePlanAssignmentTest {
    @Test
    fun `starts in planned`() {
        val item = newStrictServicePlanAssignment("code-973", "title-474")
        assertEquals(StrictServicePlanAssignmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newStrictServicePlanAssignment("code-973", "title-474")
        try {
            item.moveTo(StrictServicePlanAssignmentStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictServicePlanAssignmentStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newStrictServicePlanAssignment("code-973", "title-474")
        item.status = StrictServicePlanAssignmentStatus.CANCELLED
        assertFails("move") { item.moveTo(StrictServicePlanAssignmentStatus.PLANNED) }
    }
}
