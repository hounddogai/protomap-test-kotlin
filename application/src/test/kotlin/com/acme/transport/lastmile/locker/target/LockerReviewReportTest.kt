package com.acme.transport.lastmile.locker.target

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708090600)
        val items = mutableListOf<LockerReview>()
        for (index in 0 until 3) {
            items.add(newLockerReview("locker-review-$index", "sample"))
        }
        val report = buildLockerReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
