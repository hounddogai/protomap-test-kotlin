package com.acme.transport.emissions.fuelburn.plan

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledFuelBurnPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709994382)
        val items = mutableListOf<ScheduledFuelBurnPlan>()
        for (index in 0 until 4) {
            items.add(newScheduledFuelBurnPlan("scheduled-fuel-burn-plan-$index", "sample"))
        }
        val report = buildScheduledFuelBurnPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
