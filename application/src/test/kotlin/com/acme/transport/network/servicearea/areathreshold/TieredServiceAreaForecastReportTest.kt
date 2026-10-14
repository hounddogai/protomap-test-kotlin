package com.acme.transport.network.servicearea.areathreshold

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredServiceAreaForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702018387)
        val items = mutableListOf<TieredServiceAreaForecast>()
        for (index in 0 until 4) {
            items.add(newTieredServiceAreaForecast("tiered-service-area-forecast-$index", "sample"))
        }
        val report = buildTieredServiceAreaForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
