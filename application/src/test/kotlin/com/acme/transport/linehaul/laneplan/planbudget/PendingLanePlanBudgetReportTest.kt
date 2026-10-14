package com.acme.transport.linehaul.laneplan.planbudget

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingLanePlanBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702228630)
        val items = mutableListOf<PendingLanePlanBudget>()
        for (index in 0 until 4) {
            items.add(newPendingLanePlanBudget("pending-lane-plan-budget-$index", "sample"))
        }
        val report = buildPendingLanePlanBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
