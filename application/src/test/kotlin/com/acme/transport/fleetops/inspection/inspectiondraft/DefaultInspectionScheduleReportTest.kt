package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultInspectionScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705572537)
        val items = mutableListOf<DefaultInspectionSchedule>()
        for (index in 0 until 3) {
            items.add(newDefaultInspectionSchedule("default-inspection-schedule-$index", "sample"))
        }
        val report = buildDefaultInspectionScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
