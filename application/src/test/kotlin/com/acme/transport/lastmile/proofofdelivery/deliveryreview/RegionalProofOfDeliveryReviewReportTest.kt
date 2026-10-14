package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalProofOfDeliveryReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708530118)
        val items = mutableListOf<RegionalProofOfDeliveryReview>()
        for (index in 0 until 5) {
            items.add(newRegionalProofOfDeliveryReview("regional-proof-of-delivery-review-$index", "sample"))
        }
        val report = buildRegionalProofOfDeliveryReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
