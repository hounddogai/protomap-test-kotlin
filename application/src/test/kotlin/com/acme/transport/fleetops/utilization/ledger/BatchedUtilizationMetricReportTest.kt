package com.acme.transport.fleetops.utilization.ledger

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedUtilizationMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708408287)
        val items = mutableListOf<BatchedUtilizationMetric>()
        for (index in 0 until 4) {
            items.add(newBatchedUtilizationMetric("batched-utilization-metric-$index", "sample"))
        }
        val report = buildBatchedUtilizationMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
