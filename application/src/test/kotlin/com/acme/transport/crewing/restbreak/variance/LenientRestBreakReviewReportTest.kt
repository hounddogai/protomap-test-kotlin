package com.acme.transport.crewing.restbreak.variance

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientRestBreakReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705059845)
        val items = mutableListOf<LenientRestBreakReview>()
        for (index in 0 until 2) {
            items.add(newLenientRestBreakReview("lenient-rest-break-review-$index", "sample"))
        }
        val report = buildLenientRestBreakReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
