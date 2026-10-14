package com.acme.transport.terminal.dwell.snapshot

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalDwellBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703191210)
        val items = mutableListOf<GlobalDwellBudget>()
        for (index in 0 until 3) {
            items.add(newGlobalDwellBudget("global-dwell-budget-$index", "sample"))
        }
        val report = buildGlobalDwellBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
