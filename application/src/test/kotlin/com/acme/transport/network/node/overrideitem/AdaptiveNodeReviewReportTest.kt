package com.acme.transport.network.node.overrideitem

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveNodeReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707264057)
        val items = mutableListOf<AdaptiveNodeReview>()
        for (index in 0 until 5) {
            items.add(newAdaptiveNodeReview("adaptive-node-review-$index", "sample"))
        }
        val report = buildAdaptiveNodeReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
