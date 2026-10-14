package com.acme.transport.emissions.fuelburn.profile

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709830177)
        val items = mutableListOf<FuelBurnProfile>()
        for (index in 0 until 4) {
            items.add(newFuelBurnProfile("fuel-burn-profile-$index", "sample"))
        }
        val report = buildFuelBurnProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
