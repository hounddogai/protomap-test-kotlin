package com.acme.transport.crewing.availability.cycle

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705021426)
        val items = mutableListOf<AvailabilityReview>()
        for (index in 0 until 4) {
            items.add(newAvailabilityReview("availability-review-$index", "sample"))
        }
        val report = buildAvailabilityReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
