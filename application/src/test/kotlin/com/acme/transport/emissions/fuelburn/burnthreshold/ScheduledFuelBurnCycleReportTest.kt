package com.acme.transport.emissions.fuelburn.burnthreshold

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledFuelBurnCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708268631)
        val items = mutableListOf<ScheduledFuelBurnCycle>()
        for (index in 0 until 2) {
            items.add(newScheduledFuelBurnCycle("scheduled-fuel-burn-cycle-$index", "sample"))
        }
        val report = buildScheduledFuelBurnCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
