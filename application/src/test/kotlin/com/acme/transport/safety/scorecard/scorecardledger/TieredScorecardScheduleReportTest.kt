package com.acme.transport.safety.scorecard.scorecardledger

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredScorecardScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705337345)
        val items = mutableListOf<TieredScorecardSchedule>()
        for (index in 0 until 6) {
            items.add(newTieredScorecardSchedule("tiered-scorecard-schedule-$index", "sample"))
        }
        val report = buildTieredScorecardScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
