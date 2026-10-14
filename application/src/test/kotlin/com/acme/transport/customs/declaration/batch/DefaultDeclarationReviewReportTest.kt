package com.acme.transport.customs.declaration.batch

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultDeclarationReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703810690)
        val items = mutableListOf<DefaultDeclarationReview>()
        for (index in 0 until 2) {
            items.add(newDefaultDeclarationReview("default-declaration-review-$index", "sample"))
        }
        val report = buildDefaultDeclarationReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
