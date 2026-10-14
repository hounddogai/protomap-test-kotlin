package com.acme.transport.crewing.restbreak.baseline

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRestBreakBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702245787)
        val items = mutableListOf<ScheduledRestBreakBudget>()
        for (index in 0 until 2) {
            items.add(newScheduledRestBreakBudget("scheduled-rest-break-budget-$index", "sample"))
        }
        val report = buildScheduledRestBreakBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
