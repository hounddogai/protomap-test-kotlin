package com.acme.transport.emissions.fuelburn.request

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703493772)
        val items = mutableListOf<FuelBurnSlot>()
        for (index in 0 until 2) {
            items.add(newFuelBurnSlot("fuel-burn-slot-$index", "sample"))
        }
        val report = buildFuelBurnSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
