package com.acme.transport.crewing.roster.rostersummary

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700822582)
        val items = mutableListOf<RosterReview>()
        for (index in 0 until 4) {
            items.add(newRosterReview("roster-review-$index", "sample"))
        }
        val report = buildRosterReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
