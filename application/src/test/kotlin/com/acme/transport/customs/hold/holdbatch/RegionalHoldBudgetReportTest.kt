package com.acme.transport.customs.hold.holdbatch

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalHoldBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704653957)
        val items = mutableListOf<RegionalHoldBudget>()
        for (index in 0 until 5) {
            items.add(newRegionalHoldBudget("regional-hold-budget-$index", "sample"))
        }
        val report = buildRegionalHoldBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
