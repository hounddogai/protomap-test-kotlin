package com.acme.transport.crewing.dutyperiod.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledDutyPeriodExceptionTest {
    @Test
    fun `starts in planned`() {
        val item = newScheduledDutyPeriodException("code-333", "name-470")
        assertEquals(ScheduledDutyPeriodExceptionStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newScheduledDutyPeriodException("code-333", "name-470")
        try {
            item.moveTo(ScheduledDutyPeriodExceptionStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledDutyPeriodExceptionStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScheduledDutyPeriodException("code-333", "name-470")
        item.status = ScheduledDutyPeriodExceptionStatus.CANCELLED
        assertFails("move") { item.moveTo(ScheduledDutyPeriodExceptionStatus.PLANNED) }
    }
}
