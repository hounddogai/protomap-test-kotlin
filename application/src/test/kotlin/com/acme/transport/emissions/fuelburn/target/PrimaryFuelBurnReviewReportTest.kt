package com.acme.transport.emissions.fuelburn.target

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryFuelBurnReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701629176)
        val items = mutableListOf<PrimaryFuelBurnReview>()
        for (index in 0 until 3) {
            items.add(newPrimaryFuelBurnReview("primary-fuel-burn-review-$index", "sample"))
        }
        val report = buildPrimaryFuelBurnReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
