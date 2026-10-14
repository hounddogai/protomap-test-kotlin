package com.acme.transport.lastmile.drop.exception

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDropBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705170010)
        val items = mutableListOf<SeasonalDropBudget>()
        for (index in 0 until 5) {
            items.add(newSeasonalDropBudget("seasonal-drop-budget-$index", "sample"))
        }
        val report = buildSeasonalDropBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
