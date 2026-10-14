package com.acme.transport.crewing.swap.window

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveSwapBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709957787)
        val items = mutableListOf<AdaptiveSwapBudget>()
        for (index in 0 until 5) {
            items.add(newAdaptiveSwapBudget("adaptive-swap-budget-$index", "sample"))
        }
        val report = buildAdaptiveSwapBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
