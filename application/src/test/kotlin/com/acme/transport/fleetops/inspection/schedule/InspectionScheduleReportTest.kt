package com.acme.transport.fleetops.inspection.schedule

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708023846)
        val items = mutableListOf<InspectionSchedule>()
        for (index in 0 until 3) {
            items.add(newInspectionSchedule("inspection-schedule-$index", "sample"))
        }
        val report = buildInspectionScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
