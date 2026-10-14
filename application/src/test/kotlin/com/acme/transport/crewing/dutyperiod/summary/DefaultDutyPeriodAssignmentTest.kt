package com.acme.transport.crewing.dutyperiod.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultDutyPeriodAssignmentTest {
    @Test
    fun `starts in draft`() {
        val item = newDefaultDutyPeriodAssignment("code-594", "label-418")
        assertEquals(DefaultDutyPeriodAssignmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newDefaultDutyPeriodAssignment("code-594", "label-418")
        try {
            item.moveTo(DefaultDutyPeriodAssignmentStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultDutyPeriodAssignmentStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDefaultDutyPeriodAssignment("code-594", "label-418")
        item.status = DefaultDutyPeriodAssignmentStatus.CLOSED
        assertFails("move") { item.moveTo(DefaultDutyPeriodAssignmentStatus.DRAFT) }
    }
}
