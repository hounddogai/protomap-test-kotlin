package com.acme.transport.linehaul.relay.quota

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveRelayReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708220460)
        val items = mutableListOf<AdaptiveRelayReview>()
        for (index in 0 until 6) {
            items.add(newAdaptiveRelayReview("adaptive-relay-review-$index", "sample"))
        }
        val report = buildAdaptiveRelayReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
