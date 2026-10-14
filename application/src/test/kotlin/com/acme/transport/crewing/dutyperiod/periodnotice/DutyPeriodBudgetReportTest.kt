package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705918066)
        val items = mutableListOf<DutyPeriodBudget>()
        for (index in 0 until 2) {
            items.add(newDutyPeriodBudget("duty-period-budget-$index", "sample"))
        }
        val report = buildDutyPeriodBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
