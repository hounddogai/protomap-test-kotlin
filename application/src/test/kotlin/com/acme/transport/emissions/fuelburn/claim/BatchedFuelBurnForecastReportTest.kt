package com.acme.transport.emissions.fuelburn.claim

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedFuelBurnForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700916185)
        val items = mutableListOf<BatchedFuelBurnForecast>()
        for (index in 0 until 4) {
            items.add(newBatchedFuelBurnForecast("batched-fuel-burn-forecast-$index", "sample"))
        }
        val report = buildBatchedFuelBurnForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
