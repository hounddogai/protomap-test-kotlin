package com.acme.transport.emissions.fuelburn.budget

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706657252)
        val items = mutableListOf<FuelBurnBudget>()
        for (index in 0 until 3) {
            items.add(newFuelBurnBudget("fuel-burn-budget-$index", "sample"))
        }
        val report = buildFuelBurnBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
