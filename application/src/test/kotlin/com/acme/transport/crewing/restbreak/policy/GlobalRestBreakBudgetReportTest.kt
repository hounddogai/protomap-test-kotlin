package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRestBreakBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705246548)
        val items = mutableListOf<GlobalRestBreakBudget>()
        for (index in 0 until 4) {
            items.add(newGlobalRestBreakBudget("global-rest-break-budget-$index", "sample"))
        }
        val report = buildGlobalRestBreakBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
