package com.acme.transport.emissions.fuelburn.burnnotice

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707413576)
        val items = mutableListOf<FuelBurnMetric>()
        for (index in 0 until 5) {
            items.add(newFuelBurnMetric("fuel-burn-metric-$index", "sample"))
        }
        val report = buildFuelBurnMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
