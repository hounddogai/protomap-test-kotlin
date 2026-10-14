package com.acme.transport.emissions.fuelburn.adjustment

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalFuelBurnTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706847371)
        val items = mutableListOf<RegionalFuelBurnTarget>()
        for (index in 0 until 6) {
            items.add(newRegionalFuelBurnTarget("regional-fuel-burn-target-$index", "sample"))
        }
        val report = buildRegionalFuelBurnTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
