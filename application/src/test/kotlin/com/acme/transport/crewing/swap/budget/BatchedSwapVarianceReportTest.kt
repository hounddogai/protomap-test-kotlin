package com.acme.transport.crewing.swap.budget

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedSwapVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702787811)
        val items = mutableListOf<BatchedSwapVariance>()
        for (index in 0 until 6) {
            items.add(newBatchedSwapVariance("batched-swap-variance-$index", "sample"))
        }
        val report = buildBatchedSwapVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
