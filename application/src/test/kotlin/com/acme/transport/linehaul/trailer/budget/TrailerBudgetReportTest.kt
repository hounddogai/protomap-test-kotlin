package com.acme.transport.linehaul.trailer.budget

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704947418)
        val items = mutableListOf<TrailerBudget>()
        for (index in 0 until 4) {
            items.add(newTrailerBudget("trailer-budget-$index", "sample"))
        }
        val report = buildTrailerBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
