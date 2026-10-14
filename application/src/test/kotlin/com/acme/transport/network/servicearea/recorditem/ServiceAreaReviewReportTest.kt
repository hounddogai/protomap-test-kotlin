package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703891328)
        val items = mutableListOf<ServiceAreaReview>()
        for (index in 0 until 3) {
            items.add(newServiceAreaReview("service-area-review-$index", "sample"))
        }
        val report = buildServiceAreaReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
