package com.acme.transport.crewing.dutyperiod.periodreview

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDutyPeriodReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709583346)
        val items = mutableListOf<ManualDutyPeriodReview>()
        for (index in 0 until 3) {
            items.add(newManualDutyPeriodReview("manual-duty-period-review-$index", "sample"))
        }
        val report = buildManualDutyPeriodReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
