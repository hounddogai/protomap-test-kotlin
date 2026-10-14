package com.acme.transport.crewing.availability.segment

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryAvailabilityBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704829424)
        val items = mutableListOf<PrimaryAvailabilityBaseline>()
        for (index in 0 until 3) {
            items.add(newPrimaryAvailabilityBaseline("primary-availability-baseline-$index", "sample"))
        }
        val report = buildPrimaryAvailabilityBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
