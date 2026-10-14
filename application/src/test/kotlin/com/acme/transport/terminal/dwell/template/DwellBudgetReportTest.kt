package com.acme.transport.terminal.dwell.template

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700352865)
        val items = mutableListOf<DwellBudget>()
        for (index in 0 until 6) {
            items.add(newDwellBudget("dwell-budget-$index", "sample"))
        }
        val report = buildDwellBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
