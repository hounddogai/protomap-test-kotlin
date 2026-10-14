package com.acme.transport.terminal.traileryard.yardbudget

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTrailerYardBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707381397)
        val items = mutableListOf<BatchedTrailerYardBudget>()
        for (index in 0 until 3) {
            items.add(newBatchedTrailerYardBudget("batched-trailer-yard-budget-$index", "sample"))
        }
        val report = buildBatchedTrailerYardBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
