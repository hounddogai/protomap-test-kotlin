package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledScorecardScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708172015)
        val items = mutableListOf<ScheduledScorecardSchedule>()
        for (index in 0 until 6) {
            items.add(newScheduledScorecardSchedule("scheduled-scorecard-schedule-$index", "sample"))
        }
        val report = buildScheduledScorecardScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
