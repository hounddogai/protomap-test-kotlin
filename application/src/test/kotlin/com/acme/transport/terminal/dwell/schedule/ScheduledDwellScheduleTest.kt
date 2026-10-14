package com.acme.transport.terminal.dwell.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledDwellScheduleTest {
    @Test
    fun `starts in proposed`() {
        val item = newScheduledDwellSchedule("code-176", "name-969")
        assertEquals(ScheduledDwellScheduleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newScheduledDwellSchedule("code-176", "name-969")
        try {
            item.moveTo(ScheduledDwellScheduleStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledDwellScheduleStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newScheduledDwellSchedule("code-176", "name-969")
        item.status = ScheduledDwellScheduleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ScheduledDwellScheduleStatus.PROPOSED) }
    }
}
