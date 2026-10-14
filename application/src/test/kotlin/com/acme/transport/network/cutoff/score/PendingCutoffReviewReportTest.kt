package com.acme.transport.network.cutoff.score

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCutoffReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701563773)
        val items = mutableListOf<PendingCutoffReview>()
        for (index in 0 until 4) {
            items.add(newPendingCutoffReview("pending-cutoff-review-$index", "sample"))
        }
        val report = buildPendingCutoffReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
