package com.acme.transport.terminal.dwell.group

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultDwellBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705770611)
        val items = mutableListOf<DefaultDwellBudget>()
        for (index in 0 until 6) {
            items.add(newDefaultDwellBudget("default-dwell-budget-$index", "sample"))
        }
        val report = buildDefaultDwellBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
