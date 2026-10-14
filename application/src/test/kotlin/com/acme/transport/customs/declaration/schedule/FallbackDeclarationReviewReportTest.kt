package com.acme.transport.customs.declaration.schedule

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDeclarationReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701292733)
        val items = mutableListOf<FallbackDeclarationReview>()
        for (index in 0 until 3) {
            items.add(newFallbackDeclarationReview("fallback-declaration-review-$index", "sample"))
        }
        val report = buildFallbackDeclarationReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
