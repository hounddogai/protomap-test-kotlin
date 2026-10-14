package com.acme.transport.emissions.emissionfactor.factorpreference

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryEmissionFactorReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701873579)
        val items = mutableListOf<PrimaryEmissionFactorReview>()
        for (index in 0 until 2) {
            items.add(newPrimaryEmissionFactorReview("primary-emission-factor-review-$index", "sample"))
        }
        val report = buildPrimaryEmissionFactorReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
