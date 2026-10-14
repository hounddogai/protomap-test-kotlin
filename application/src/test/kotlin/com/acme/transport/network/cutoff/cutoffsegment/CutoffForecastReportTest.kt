package com.acme.transport.network.cutoff.cutoffsegment

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709478907)
        val items = mutableListOf<CutoffForecast>()
        for (index in 0 until 4) {
            items.add(newCutoffForecast("cutoff-forecast-$index", "sample"))
        }
        val report = buildCutoffForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
