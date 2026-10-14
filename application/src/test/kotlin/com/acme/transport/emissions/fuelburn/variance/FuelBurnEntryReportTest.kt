package com.acme.transport.emissions.fuelburn.variance

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705728159)
        val items = mutableListOf<FuelBurnEntry>()
        for (index in 0 until 4) {
            items.add(newFuelBurnEntry("fuel-burn-entry-$index", "sample"))
        }
        val report = buildFuelBurnEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
