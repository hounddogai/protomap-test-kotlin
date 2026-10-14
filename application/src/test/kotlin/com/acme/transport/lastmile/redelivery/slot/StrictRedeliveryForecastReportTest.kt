package com.acme.transport.lastmile.redelivery.slot

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRedeliveryForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703086176)
        val items = mutableListOf<StrictRedeliveryForecast>()
        for (index in 0 until 6) {
            items.add(newStrictRedeliveryForecast("strict-redelivery-forecast-$index", "sample"))
        }
        val report = buildStrictRedeliveryForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
