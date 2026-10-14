package com.acme.transport.crewing.dutyperiod.manualperiodassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedDutyPeriodAssignmentTest {
    @Test
    fun `starts in planned`() {
        val item = newCappedDutyPeriodAssignment("code-886", "title-884")
        assertEquals(CappedDutyPeriodAssignmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newCappedDutyPeriodAssignment("code-886", "title-884")
        try {
            item.moveTo(CappedDutyPeriodAssignmentStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedDutyPeriodAssignmentStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCappedDutyPeriodAssignment("code-886", "title-884")
        item.status = CappedDutyPeriodAssignmentStatus.CANCELLED
        assertFails("move") { item.moveTo(CappedDutyPeriodAssignmentStatus.PLANNED) }
    }
}
