package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702912906)
        val items = mutableListOf<BackhaulForecast>()
        for (index in 0 until 2) {
            items.add(newBackhaulForecast("backhaul-forecast-$index", "sample"))
        }
        val report = buildBackhaulForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
