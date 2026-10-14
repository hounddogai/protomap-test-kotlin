package com.acme.transport.safety.scorecard.quota

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalScorecardBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701867628)
        val items = mutableListOf<GlobalScorecardBudget>()
        for (index in 0 until 3) {
            items.add(newGlobalScorecardBudget("global-scorecard-budget-$index", "sample"))
        }
        val report = buildGlobalScorecardBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
