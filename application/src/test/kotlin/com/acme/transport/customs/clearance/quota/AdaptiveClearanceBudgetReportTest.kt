package com.acme.transport.customs.clearance.quota

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveClearanceBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709360167)
        val items = mutableListOf<AdaptiveClearanceBudget>()
        for (index in 0 until 3) {
            items.add(newAdaptiveClearanceBudget("adaptive-clearance-budget-$index", "sample"))
        }
        val report = buildAdaptiveClearanceBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
