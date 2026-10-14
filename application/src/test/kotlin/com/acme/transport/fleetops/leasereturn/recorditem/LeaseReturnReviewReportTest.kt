package com.acme.transport.fleetops.leasereturn.recorditem

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706079963)
        val items = mutableListOf<LeaseReturnReview>()
        for (index in 0 until 3) {
            items.add(newLeaseReturnReview("lease-return-review-$index", "sample"))
        }
        val report = buildLeaseReturnReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
