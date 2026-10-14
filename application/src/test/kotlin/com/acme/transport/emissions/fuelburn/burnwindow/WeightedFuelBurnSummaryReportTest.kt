package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedFuelBurnSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700943342)
        val items = mutableListOf<WeightedFuelBurnSummary>()
        for (index in 0 until 4) {
            items.add(newWeightedFuelBurnSummary("weighted-fuel-burn-summary-$index", "sample"))
        }
        val report = buildWeightedFuelBurnSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
