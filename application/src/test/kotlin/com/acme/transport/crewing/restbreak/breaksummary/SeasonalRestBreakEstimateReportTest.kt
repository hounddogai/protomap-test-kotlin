package com.acme.transport.crewing.restbreak.breaksummary

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRestBreakEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701465734)
        val items = mutableListOf<SeasonalRestBreakEstimate>()
        for (index in 0 until 5) {
            items.add(newSeasonalRestBreakEstimate("seasonal-rest-break-estimate-$index", "sample"))
        }
        val report = buildSeasonalRestBreakEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
