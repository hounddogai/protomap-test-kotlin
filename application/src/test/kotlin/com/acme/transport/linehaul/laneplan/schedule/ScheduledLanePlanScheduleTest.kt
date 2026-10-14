package com.acme.transport.linehaul.laneplan.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledLanePlanScheduleTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledLanePlanSchedule("code-235", "label-442")
        assertEquals(ScheduledLanePlanScheduleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newScheduledLanePlanSchedule("code-235", "label-442")
        try {
            item.moveTo(ScheduledLanePlanScheduleStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledLanePlanScheduleStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledLanePlanSchedule("code-235", "label-442")
        item.status = ScheduledLanePlanScheduleStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledLanePlanScheduleStatus.CREATED) }
    }
}
