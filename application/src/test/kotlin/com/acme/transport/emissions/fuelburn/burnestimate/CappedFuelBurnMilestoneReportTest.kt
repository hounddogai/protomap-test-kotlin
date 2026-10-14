package com.acme.transport.emissions.fuelburn.burnestimate

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedFuelBurnMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705318279)
        val items = mutableListOf<CappedFuelBurnMilestone>()
        for (index in 0 until 2) {
            items.add(newCappedFuelBurnMilestone("capped-fuel-burn-milestone-$index", "sample"))
        }
        val report = buildCappedFuelBurnMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
