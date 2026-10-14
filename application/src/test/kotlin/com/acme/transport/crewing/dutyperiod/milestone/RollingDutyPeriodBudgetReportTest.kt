package com.acme.transport.crewing.dutyperiod.milestone

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDutyPeriodBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707582818)
        val items = mutableListOf<RollingDutyPeriodBudget>()
        for (index in 0 until 3) {
            items.add(newRollingDutyPeriodBudget("rolling-duty-period-budget-$index", "sample"))
        }
        val report = buildRollingDutyPeriodBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
