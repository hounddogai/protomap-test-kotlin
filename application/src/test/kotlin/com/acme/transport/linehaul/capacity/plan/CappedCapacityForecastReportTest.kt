package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedCapacityForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700618280)
        val items = mutableListOf<CappedCapacityForecast>()
        for (index in 0 until 6) {
            items.add(newCappedCapacityForecast("capped-capacity-forecast-$index", "sample"))
        }
        val report = buildCappedCapacityForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
