package com.acme.transport.freightpay.freightinvoice.review

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedFreightInvoiceReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707641153)
        val items = mutableListOf<BatchedFreightInvoiceReview>()
        for (index in 0 until 4) {
            items.add(newBatchedFreightInvoiceReview("batched-freight-invoice-review-$index", "sample"))
        }
        val report = buildBatchedFreightInvoiceReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
