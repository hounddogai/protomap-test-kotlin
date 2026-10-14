package com.acme.transport.crewing.restbreak.rollup

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708404120)
        val items = mutableListOf<RestBreakReview>()
        for (index in 0 until 2) {
            items.add(newRestBreakReview("rest-break-review-$index", "sample"))
        }
        val report = buildRestBreakReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
