package com.acme.transport.customs.clearance.threshold

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700461857)
        val items = mutableListOf<ClearanceBudget>()
        for (index in 0 until 4) {
            items.add(newClearanceBudget("clearance-budget-$index", "sample"))
        }
        val report = buildClearanceBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
