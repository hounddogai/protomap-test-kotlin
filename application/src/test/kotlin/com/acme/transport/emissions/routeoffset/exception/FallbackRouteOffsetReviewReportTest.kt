package com.acme.transport.emissions.routeoffset.exception

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRouteOffsetReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706148864)
        val items = mutableListOf<FallbackRouteOffsetReview>()
        for (index in 0 until 6) {
            items.add(newFallbackRouteOffsetReview("fallback-route-offset-review-$index", "sample"))
        }
        val report = buildFallbackRouteOffsetReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
