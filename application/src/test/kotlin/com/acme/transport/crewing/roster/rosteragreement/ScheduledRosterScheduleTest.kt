package com.acme.transport.crewing.roster.rosteragreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledRosterScheduleTest {
    @Test
    fun `starts in planned`() {
        val item = newScheduledRosterSchedule("code-148", "label-811")
        assertEquals(ScheduledRosterScheduleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newScheduledRosterSchedule("code-148", "label-811")
        try {
            item.moveTo(ScheduledRosterScheduleStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledRosterScheduleStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScheduledRosterSchedule("code-148", "label-811")
        item.status = ScheduledRosterScheduleStatus.CANCELLED
        assertFails("move") { item.moveTo(ScheduledRosterScheduleStatus.PLANNED) }
    }
}
