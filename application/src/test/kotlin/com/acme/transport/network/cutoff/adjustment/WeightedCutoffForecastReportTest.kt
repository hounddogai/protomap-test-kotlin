package com.acme.transport.network.cutoff.adjustment

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedCutoffForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700634585)
        val items = mutableListOf<WeightedCutoffForecast>()
        for (index in 0 until 5) {
            items.add(newWeightedCutoffForecast("weighted-cutoff-forecast-$index", "sample"))
        }
        val report = buildWeightedCutoffForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
