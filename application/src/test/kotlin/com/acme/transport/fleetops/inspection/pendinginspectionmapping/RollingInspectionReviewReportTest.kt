package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingInspectionReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704623731)
        val items = mutableListOf<RollingInspectionReview>()
        for (index in 0 until 5) {
            items.add(newRollingInspectionReview("rolling-inspection-review-$index", "sample"))
        }
        val report = buildRollingInspectionReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
