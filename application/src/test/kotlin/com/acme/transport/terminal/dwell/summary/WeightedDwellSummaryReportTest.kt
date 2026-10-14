package com.acme.transport.terminal.dwell.summary

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedDwellSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701349110)
        val items = mutableListOf<WeightedDwellSummary>()
        for (index in 0 until 2) {
            items.add(newWeightedDwellSummary("weighted-dwell-summary-$index", "sample"))
        }
        val report = buildWeightedDwellSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
