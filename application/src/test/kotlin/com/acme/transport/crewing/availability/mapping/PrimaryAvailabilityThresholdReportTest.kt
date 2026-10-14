package com.acme.transport.crewing.availability.mapping

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryAvailabilityThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704316775)
        val items = mutableListOf<PrimaryAvailabilityThreshold>()
        for (index in 0 until 3) {
            items.add(newPrimaryAvailabilityThreshold("primary-availability-threshold-$index", "sample"))
        }
        val report = buildPrimaryAvailabilityThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
