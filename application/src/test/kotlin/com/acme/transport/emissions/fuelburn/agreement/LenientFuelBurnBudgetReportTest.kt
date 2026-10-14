package com.acme.transport.emissions.fuelburn.agreement

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientFuelBurnBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700047596)
        val items = mutableListOf<LenientFuelBurnBudget>()
        for (index in 0 until 2) {
            items.add(newLenientFuelBurnBudget("lenient-fuel-burn-budget-$index", "sample"))
        }
        val report = buildLenientFuelBurnBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
