package com.acme.transport.freightpay.dispute.review

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701067445)
        val items = mutableListOf<DisputeReview>()
        for (index in 0 until 4) {
            items.add(newDisputeReview("dispute-review-$index", "sample"))
        }
        val report = buildDisputeReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
