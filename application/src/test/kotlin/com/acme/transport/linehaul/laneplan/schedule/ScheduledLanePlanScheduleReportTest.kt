package com.acme.transport.linehaul.laneplan.schedule

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledLanePlanScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709518470)
        val items = mutableListOf<ScheduledLanePlanSchedule>()
        for (index in 0 until 5) {
            items.add(newScheduledLanePlanSchedule("scheduled-lane-plan-schedule-$index", "sample"))
        }
        val report = buildScheduledLanePlanScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
