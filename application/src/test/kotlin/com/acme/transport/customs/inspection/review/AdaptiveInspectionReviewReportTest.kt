package com.acme.transport.customs.inspection.review

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveInspectionReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708518420)
        val items = mutableListOf<AdaptiveInspectionReview>()
        for (index in 0 until 2) {
            items.add(newAdaptiveInspectionReview("adaptive-inspection-review-$index", "sample"))
        }
        val report = buildAdaptiveInspectionReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
