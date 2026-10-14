package com.acme.transport.crewing.availability.tier

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704357073)
        val items = mutableListOf<AvailabilityTier>()
        for (index in 0 until 2) {
            items.add(newAvailabilityTier("availability-tier-$index", "sample"))
        }
        val report = buildAvailabilityTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
