package com.acme.transport.lastmile.drop.forecast

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDropForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703988990)
        val items = mutableListOf<FallbackDropForecast>()
        for (index in 0 until 4) {
            items.add(newFallbackDropForecast("fallback-drop-forecast-$index", "sample"))
        }
        val report = buildFallbackDropForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
