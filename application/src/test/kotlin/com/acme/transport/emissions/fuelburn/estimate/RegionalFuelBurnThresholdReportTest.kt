package com.acme.transport.emissions.fuelburn.estimate

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalFuelBurnThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703958536)
        val items = mutableListOf<RegionalFuelBurnThreshold>()
        for (index in 0 until 2) {
            items.add(newRegionalFuelBurnThreshold("regional-fuel-burn-threshold-$index", "sample"))
        }
        val report = buildRegionalFuelBurnThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
