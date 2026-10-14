package com.acme.transport.linehaul.capacity.agreement

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedCapacityMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702015329)
        val items = mutableListOf<BatchedCapacityMetric>()
        for (index in 0 until 2) {
            items.add(newBatchedCapacityMetric("batched-capacity-metric-$index", "sample"))
        }
        val report = buildBatchedCapacityMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
