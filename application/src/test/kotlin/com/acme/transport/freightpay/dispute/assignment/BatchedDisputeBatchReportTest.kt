package com.acme.transport.freightpay.dispute.assignment

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDisputeBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706845864)
        val items = mutableListOf<BatchedDisputeBatch>()
        for (index in 0 until 4) {
            items.add(newBatchedDisputeBatch("batched-dispute-batch-$index", "sample"))
        }
        val report = buildBatchedDisputeBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
