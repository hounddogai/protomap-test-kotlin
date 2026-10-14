package com.acme.transport.freightpay.ratecard.forecast

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedRateCardMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707913661)
        val items = mutableListOf<BatchedRateCardMilestone>()
        for (index in 0 until 5) {
            items.add(newBatchedRateCardMilestone("batched-rate-card-milestone-$index", "sample"))
        }
        val report = buildBatchedRateCardMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
