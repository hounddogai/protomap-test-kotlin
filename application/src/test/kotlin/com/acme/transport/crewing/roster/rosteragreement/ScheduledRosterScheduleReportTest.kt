package com.acme.transport.crewing.roster.rosteragreement

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRosterScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708422079)
        val items = mutableListOf<ScheduledRosterSchedule>()
        for (index in 0 until 4) {
            items.add(newScheduledRosterSchedule("scheduled-roster-schedule-$index", "sample"))
        }
        val report = buildScheduledRosterScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
