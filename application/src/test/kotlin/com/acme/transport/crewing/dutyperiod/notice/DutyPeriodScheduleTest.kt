package com.acme.transport.crewing.dutyperiod.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodScheduleTest {
    @Test
    fun `starts in new`() {
        val item = newDutyPeriodSchedule("code-932", "label-594")
        assertEquals(DutyPeriodScheduleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newDutyPeriodSchedule("code-932", "label-594")
        try {
            item.moveTo(DutyPeriodScheduleStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodScheduleStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDutyPeriodSchedule("code-932", "label-594")
        item.status = DutyPeriodScheduleStatus.FAILED
        assertFails("move") { item.moveTo(DutyPeriodScheduleStatus.NEW) }
    }
}
