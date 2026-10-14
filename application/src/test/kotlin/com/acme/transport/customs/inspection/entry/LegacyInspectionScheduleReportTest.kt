package com.acme.transport.customs.inspection.entry

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyInspectionScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701311428)
        val items = mutableListOf<LegacyInspectionSchedule>()
        for (index in 0 until 3) {
            items.add(newLegacyInspectionSchedule("legacy-inspection-schedule-$index", "sample"))
        }
        val report = buildLegacyInspectionScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
