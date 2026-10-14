package com.acme.transport.crewing.dutyperiod.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodAssignmentTest {
    @Test
    fun `starts in planned`() {
        val item = newDutyPeriodAssignment("code-991", "label-930")
        assertEquals(DutyPeriodAssignmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newDutyPeriodAssignment("code-991", "label-930")
        try {
            item.moveTo(DutyPeriodAssignmentStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodAssignmentStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDutyPeriodAssignment("code-991", "label-930")
        item.status = DutyPeriodAssignmentStatus.CANCELLED
        assertFails("move") { item.moveTo(DutyPeriodAssignmentStatus.PLANNED) }
    }
}
