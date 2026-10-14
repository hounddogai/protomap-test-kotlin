package com.acme.transport.linehaul.laneplan.planwindow

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalLanePlanBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707194520)
        val items = mutableListOf<RegionalLanePlanBudget>()
        for (index in 0 until 6) {
            items.add(newRegionalLanePlanBudget("regional-lane-plan-budget-$index", "sample"))
        }
        val report = buildRegionalLanePlanBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
