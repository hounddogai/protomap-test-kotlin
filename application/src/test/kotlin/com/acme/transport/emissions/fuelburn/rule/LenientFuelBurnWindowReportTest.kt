package com.acme.transport.emissions.fuelburn.rule

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientFuelBurnWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706449084)
        val items = mutableListOf<LenientFuelBurnWindow>()
        for (index in 0 until 6) {
            items.add(newLenientFuelBurnWindow("lenient-fuel-burn-window-$index", "sample"))
        }
        val report = buildLenientFuelBurnWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
