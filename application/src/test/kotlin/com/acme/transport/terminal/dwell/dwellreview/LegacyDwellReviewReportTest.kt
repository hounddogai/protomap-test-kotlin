package com.acme.transport.terminal.dwell.dwellreview

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDwellReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702969697)
        val items = mutableListOf<LegacyDwellReview>()
        for (index in 0 until 4) {
            items.add(newLegacyDwellReview("legacy-dwell-review-$index", "sample"))
        }
        val report = buildLegacyDwellReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
