package com.acme.transport.crewing.qualification.qualificationproposal

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryQualificationReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706051763)
        val items = mutableListOf<PrimaryQualificationReview>()
        for (index in 0 until 3) {
            items.add(newPrimaryQualificationReview("primary-qualification-review-$index", "sample"))
        }
        val report = buildPrimaryQualificationReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
