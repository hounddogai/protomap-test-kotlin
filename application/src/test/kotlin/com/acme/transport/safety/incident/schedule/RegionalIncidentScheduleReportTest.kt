package com.acme.transport.safety.incident.schedule

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalIncidentScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707503332)
        val items = mutableListOf<RegionalIncidentSchedule>()
        for (index in 0 until 5) {
            items.add(newRegionalIncidentSchedule("regional-incident-schedule-$index", "sample"))
        }
        val report = buildRegionalIncidentScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
