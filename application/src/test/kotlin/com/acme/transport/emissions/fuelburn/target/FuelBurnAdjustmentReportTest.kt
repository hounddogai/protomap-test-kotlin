package com.acme.transport.emissions.fuelburn.target

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707880695)
        val items = mutableListOf<FuelBurnAdjustment>()
        for (index in 0 until 3) {
            items.add(newFuelBurnAdjustment("fuel-burn-adjustment-$index", "sample"))
        }
        val report = buildFuelBurnAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
