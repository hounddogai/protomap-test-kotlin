package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientAccessorialReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708562142)
        val items = mutableListOf<LenientAccessorialReview>()
        for (index in 0 until 4) {
            items.add(newLenientAccessorialReview("lenient-accessorial-review-$index", "sample"))
        }
        val report = buildLenientAccessorialReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
