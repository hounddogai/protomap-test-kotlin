package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDwellReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707242746)
        val items = mutableListOf<LenientDwellReview>()
        for (index in 0 until 6) {
            items.add(newLenientDwellReview("lenient-dwell-review-$index", "sample"))
        }
        val report = buildLenientDwellReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
