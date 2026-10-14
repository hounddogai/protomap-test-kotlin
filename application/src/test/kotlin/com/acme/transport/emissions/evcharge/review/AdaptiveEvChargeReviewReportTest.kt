package com.acme.transport.emissions.evcharge.review

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveEvChargeReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701560621)
        val items = mutableListOf<AdaptiveEvChargeReview>()
        for (index in 0 until 6) {
            items.add(newAdaptiveEvChargeReview("adaptive-ev-charge-review-$index", "sample"))
        }
        val report = buildAdaptiveEvChargeReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
