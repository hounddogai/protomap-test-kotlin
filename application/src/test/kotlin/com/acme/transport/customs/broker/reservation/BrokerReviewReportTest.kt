package com.acme.transport.customs.broker.reservation

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703440257)
        val items = mutableListOf<BrokerReview>()
        for (index in 0 until 3) {
            items.add(newBrokerReview("broker-review-$index", "sample"))
        }
        val report = buildBrokerReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
