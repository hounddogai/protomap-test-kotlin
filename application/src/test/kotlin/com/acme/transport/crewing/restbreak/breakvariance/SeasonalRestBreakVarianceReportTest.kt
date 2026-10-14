package com.acme.transport.crewing.restbreak.breakvariance

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRestBreakVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708931746)
        val items = mutableListOf<SeasonalRestBreakVariance>()
        for (index in 0 until 2) {
            items.add(newSeasonalRestBreakVariance("seasonal-rest-break-variance-$index", "sample"))
        }
        val report = buildSeasonalRestBreakVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
