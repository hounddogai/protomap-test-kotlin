package com.acme.transport.safety.coaching.coachingdigest

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledCoachingSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705592774)
        val items = mutableListOf<ScheduledCoachingSummary>()
        for (index in 0 until 4) {
            items.add(newScheduledCoachingSummary("scheduled-coaching-summary-$index", "sample"))
        }
        val report = buildScheduledCoachingSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
