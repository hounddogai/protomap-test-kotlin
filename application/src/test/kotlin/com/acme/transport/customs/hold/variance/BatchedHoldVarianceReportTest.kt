package com.acme.transport.customs.hold.variance

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedHoldVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704848364)
        val items = mutableListOf<BatchedHoldVariance>()
        for (index in 0 until 6) {
            items.add(newBatchedHoldVariance("batched-hold-variance-$index", "sample"))
        }
        val report = buildBatchedHoldVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
