package com.acme.transport.crewing.dutyperiod.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDutyPeriodScheduleTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalDutyPeriodSchedule("code-997", "name-369")
        assertEquals(SeasonalDutyPeriodScheduleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newSeasonalDutyPeriodSchedule("code-997", "name-369")
        try {
            item.moveTo(SeasonalDutyPeriodScheduleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDutyPeriodScheduleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalDutyPeriodSchedule("code-997", "name-369")
        item.status = SeasonalDutyPeriodScheduleStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalDutyPeriodScheduleStatus.REQUESTED) }
    }
}
