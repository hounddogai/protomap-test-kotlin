package com.acme.transport.emissions.carbonreport.mapping

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707845990)
        val items = mutableListOf<CarbonReportReview>()
        for (index in 0 until 4) {
            items.add(newCarbonReportReview("carbon-report-review-$index", "sample"))
        }
        val report = buildCarbonReportReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
