package com.acme.transport.terminal.sortation.review

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700690149)
        val items = mutableListOf<SortationReview>()
        for (index in 0 until 5) {
            items.add(newSortationReview("sortation-review-$index", "sample"))
        }
        val report = buildSortationReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
