package com.acme.transport.crewing.roster.budget

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRosterBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704849278)
        val items = mutableListOf<DefaultRosterBudget>()
        for (index in 0 until 4) {
            items.add(newDefaultRosterBudget("default-roster-budget-$index", "sample"))
        }
        val report = buildDefaultRosterBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
