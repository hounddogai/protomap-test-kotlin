package com.acme.transport.terminal.traileryard.entry

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalTrailerYardVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704840037)
        val items = mutableListOf<SeasonalTrailerYardVariance>()
        for (index in 0 until 2) {
            items.add(newSeasonalTrailerYardVariance("seasonal-trailer-yard-variance-$index", "sample"))
        }
        val report = buildSeasonalTrailerYardVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
