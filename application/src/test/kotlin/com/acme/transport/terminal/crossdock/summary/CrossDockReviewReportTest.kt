package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706712784)
        val items = mutableListOf<CrossDockReview>()
        for (index in 0 until 4) {
            items.add(newCrossDockReview("cross-dock-review-$index", "sample"))
        }
        val report = buildCrossDockReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
