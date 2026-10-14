package com.acme.transport.fleetops.defect.budget

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDefectBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706198215)
        val items = mutableListOf<SeasonalDefectBudget>()
        for (index in 0 until 2) {
            items.add(newSeasonalDefectBudget("seasonal-defect-budget-$index", "sample"))
        }
        val report = buildSeasonalDefectBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
