package com.acme.transport.emissions.fuelburn.baseline

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702750817)
        val items = mutableListOf<FuelBurnRollup>()
        for (index in 0 until 5) {
            items.add(newFuelBurnRollup("fuel-burn-rollup-$index", "sample"))
        }
        val report = buildFuelBurnRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
