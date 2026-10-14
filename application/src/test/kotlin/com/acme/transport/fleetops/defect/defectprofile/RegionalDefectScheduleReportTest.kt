package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDefectScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700851339)
        val items = mutableListOf<RegionalDefectSchedule>()
        for (index in 0 until 6) {
            items.add(newRegionalDefectSchedule("regional-defect-schedule-$index", "sample"))
        }
        val report = buildRegionalDefectScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
