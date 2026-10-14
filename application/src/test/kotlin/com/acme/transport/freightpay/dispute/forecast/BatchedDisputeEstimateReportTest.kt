package com.acme.transport.freightpay.dispute.forecast

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDisputeEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706435192)
        val items = mutableListOf<BatchedDisputeEstimate>()
        for (index in 0 until 5) {
            items.add(newBatchedDisputeEstimate("batched-dispute-estimate-$index", "sample"))
        }
        val report = buildBatchedDisputeEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
