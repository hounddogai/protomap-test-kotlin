package com.acme.transport.network.servicearea.areatarget

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientServiceAreaReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706857477)
        val items = mutableListOf<LenientServiceAreaReview>()
        for (index in 0 until 2) {
            items.add(newLenientServiceAreaReview("lenient-service-area-review-$index", "sample"))
        }
        val report = buildLenientServiceAreaReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
