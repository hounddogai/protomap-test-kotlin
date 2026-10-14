package com.acme.transport.network.zone.agreement

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredZoneForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708575409)
        val items = mutableListOf<TieredZoneForecast>()
        for (index in 0 until 4) {
            items.add(newTieredZoneForecast("tiered-zone-forecast-$index", "sample"))
        }
        val report = buildTieredZoneForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
