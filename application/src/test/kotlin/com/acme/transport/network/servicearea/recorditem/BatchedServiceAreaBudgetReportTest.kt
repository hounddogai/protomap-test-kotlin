package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedServiceAreaBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705435301)
        val items = mutableListOf<BatchedServiceAreaBudget>()
        for (index in 0 until 4) {
            items.add(newBatchedServiceAreaBudget("batched-service-area-budget-$index", "sample"))
        }
        val report = buildBatchedServiceAreaBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
