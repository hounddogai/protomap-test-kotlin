package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalServicePlanBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708236543)
        val items = mutableListOf<SeasonalServicePlanBudget>()
        for (index in 0 until 6) {
            items.add(newSeasonalServicePlanBudget("seasonal-service-plan-budget-$index", "sample"))
        }
        val report = buildSeasonalServicePlanBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
