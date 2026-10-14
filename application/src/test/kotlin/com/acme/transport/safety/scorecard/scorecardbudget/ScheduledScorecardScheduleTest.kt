package com.acme.transport.safety.scorecard.scorecardbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledScorecardScheduleTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledScorecardSchedule("code-501", "title-591")
        assertEquals(ScheduledScorecardScheduleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newScheduledScorecardSchedule("code-501", "title-591")
        try {
            item.moveTo(ScheduledScorecardScheduleStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledScorecardScheduleStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledScorecardSchedule("code-501", "title-591")
        item.status = ScheduledScorecardScheduleStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledScorecardScheduleStatus.NEW) }
    }
}
