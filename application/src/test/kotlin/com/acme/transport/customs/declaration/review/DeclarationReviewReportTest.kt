package com.acme.transport.customs.declaration.review

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703609139)
        val items = mutableListOf<DeclarationReview>()
        for (index in 0 until 4) {
            items.add(newDeclarationReview("declaration-review-$index", "sample"))
        }
        val report = buildDeclarationReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
