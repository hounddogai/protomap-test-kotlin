package com.acme.transport.crewing.dutyperiod.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodTargetTest {
    @Test
    fun `starts in planned`() {
        val item = newDutyPeriodTarget("code-467", "label-123")
        assertEquals(DutyPeriodTargetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newDutyPeriodTarget("code-467", "label-123")
        try {
            item.moveTo(DutyPeriodTargetStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodTargetStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDutyPeriodTarget("code-467", "label-123")
        item.status = DutyPeriodTargetStatus.CANCELLED
        assertFails("move") { item.moveTo(DutyPeriodTargetStatus.PLANNED) }
    }
}
