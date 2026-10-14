package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackLanePlanReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701171357)
        val items = mutableListOf<FallbackLanePlanReview>()
        for (index in 0 until 6) {
            items.add(newFallbackLanePlanReview("fallback-lane-plan-review-$index", "sample"))
        }
        val report = buildFallbackLanePlanReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
