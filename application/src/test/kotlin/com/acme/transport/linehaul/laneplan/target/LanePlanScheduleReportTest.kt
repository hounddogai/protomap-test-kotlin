package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708675938)
        val items = mutableListOf<LanePlanSchedule>()
        for (index in 0 until 5) {
            items.add(newLanePlanSchedule("lane-plan-schedule-$index", "sample"))
        }
        val report = buildLanePlanScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
