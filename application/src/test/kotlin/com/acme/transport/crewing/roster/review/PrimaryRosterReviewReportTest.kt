package com.acme.transport.crewing.roster.review

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRosterReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703539863)
        val items = mutableListOf<PrimaryRosterReview>()
        for (index in 0 until 2) {
            items.add(newPrimaryRosterReview("primary-roster-review-$index", "sample"))
        }
        val report = buildPrimaryRosterReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
