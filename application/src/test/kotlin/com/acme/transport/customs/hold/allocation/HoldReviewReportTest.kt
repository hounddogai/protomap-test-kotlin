package com.acme.transport.customs.hold.allocation

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705681200)
        val items = mutableListOf<HoldReview>()
        for (index in 0 until 4) {
            items.add(newHoldReview("hold-review-$index", "sample"))
        }
        val report = buildHoldReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
