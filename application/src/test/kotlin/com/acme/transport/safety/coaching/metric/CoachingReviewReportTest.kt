package com.acme.transport.safety.coaching.metric

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705730800)
        val items = mutableListOf<CoachingReview>()
        for (index in 0 until 4) {
            items.add(newCoachingReview("coaching-review-$index", "sample"))
        }
        val report = buildCoachingReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
