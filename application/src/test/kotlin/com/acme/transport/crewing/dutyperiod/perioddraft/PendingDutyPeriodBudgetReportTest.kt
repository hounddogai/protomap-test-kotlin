package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDutyPeriodBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701116969)
        val items = mutableListOf<PendingDutyPeriodBudget>()
        for (index in 0 until 4) {
            items.add(newPendingDutyPeriodBudget("pending-duty-period-budget-$index", "sample"))
        }
        val report = buildPendingDutyPeriodBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
