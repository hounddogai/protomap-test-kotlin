package com.acme.transport.freightpay.accessorial.accessorialscore

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalAccessorialMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707961307)
        val items = mutableListOf<SeasonalAccessorialMetric>()
        for (index in 0 until 2) {
            items.add(newSeasonalAccessorialMetric("seasonal-accessorial-metric-$index", "sample"))
        }
        val report = buildSeasonalAccessorialMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
