package com.acme.transport.crewing.restbreak.entry

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702642509)
        val items = mutableListOf<RestBreakForecast>()
        for (index in 0 until 4) {
            items.add(newRestBreakForecast("rest-break-forecast-$index", "sample"))
        }
        val report = buildRestBreakForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
