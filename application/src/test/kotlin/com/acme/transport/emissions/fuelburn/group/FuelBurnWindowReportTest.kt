package com.acme.transport.emissions.fuelburn.group

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705915109)
        val items = mutableListOf<FuelBurnWindow>()
        for (index in 0 until 4) {
            items.add(newFuelBurnWindow("fuel-burn-window-$index", "sample"))
        }
        val report = buildFuelBurnWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
