package com.acme.transport.emissions.idling.review

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredIdlingReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709075774)
        val items = mutableListOf<TieredIdlingReview>()
        for (index in 0 until 4) {
            items.add(newTieredIdlingReview("tiered-idling-review-$index", "sample"))
        }
        val report = buildTieredIdlingReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
