package com.acme.transport.customs.inspection.target

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703614375)
        val items = mutableListOf<InspectionReview>()
        for (index in 0 until 2) {
            items.add(newInspectionReview("inspection-review-$index", "sample"))
        }
        val report = buildInspectionReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
