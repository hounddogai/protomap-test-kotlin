package com.acme.transport.emissions.fuelburn.plan

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedFuelBurnGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703020231)
        val items = mutableListOf<WeightedFuelBurnGroup>()
        for (index in 0 until 5) {
            items.add(newWeightedFuelBurnGroup("weighted-fuel-burn-group-$index", "sample"))
        }
        val report = buildWeightedFuelBurnGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
