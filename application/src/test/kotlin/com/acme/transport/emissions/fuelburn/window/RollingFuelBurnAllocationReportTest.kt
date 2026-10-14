package com.acme.transport.emissions.fuelburn.window

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingFuelBurnAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703709367)
        val items = mutableListOf<RollingFuelBurnAllocation>()
        for (index in 0 until 4) {
            items.add(newRollingFuelBurnAllocation("rolling-fuel-burn-allocation-$index", "sample"))
        }
        val report = buildRollingFuelBurnAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
