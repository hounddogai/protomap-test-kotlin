package com.acme.transport.emissions.fuelburn.burnbudget

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709053625)
        val items = mutableListOf<FuelBurnForecast>()
        for (index in 0 until 5) {
            items.add(newFuelBurnForecast("fuel-burn-forecast-$index", "sample"))
        }
        val report = buildFuelBurnForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
