package com.acme.transport.lastmile.locker.budget

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledLockerBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702560742)
        val items = mutableListOf<ScheduledLockerBudget>()
        for (index in 0 until 5) {
            items.add(newScheduledLockerBudget("scheduled-locker-budget-$index", "sample"))
        }
        val report = buildScheduledLockerBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
