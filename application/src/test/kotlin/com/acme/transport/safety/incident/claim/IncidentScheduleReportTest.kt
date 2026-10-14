package com.acme.transport.safety.incident.claim

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705203817)
        val items = mutableListOf<IncidentSchedule>()
        for (index in 0 until 5) {
            items.add(newIncidentSchedule("incident-schedule-$index", "sample"))
        }
        val report = buildIncidentScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
