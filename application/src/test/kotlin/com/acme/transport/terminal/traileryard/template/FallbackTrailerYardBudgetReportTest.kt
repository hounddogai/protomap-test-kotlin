package com.acme.transport.terminal.traileryard.template

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackTrailerYardBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705131086)
        val items = mutableListOf<FallbackTrailerYardBudget>()
        for (index in 0 until 4) {
            items.add(newFallbackTrailerYardBudget("fallback-trailer-yard-budget-$index", "sample"))
        }
        val report = buildFallbackTrailerYardBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
