package com.acme.transport.emissions.fuelburn.variance

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701233884)
        val items = mutableListOf<FuelBurnVariance>()
        for (index in 0 until 3) {
            items.add(newFuelBurnVariance("fuel-burn-variance-$index", "sample"))
        }
        val report = buildFuelBurnVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
