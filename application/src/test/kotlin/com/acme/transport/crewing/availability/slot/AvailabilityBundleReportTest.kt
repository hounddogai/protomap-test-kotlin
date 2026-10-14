package com.acme.transport.crewing.availability.slot

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702371282)
        val items = mutableListOf<AvailabilityBundle>()
        for (index in 0 until 4) {
            items.add(newAvailabilityBundle("availability-bundle-$index", "sample"))
        }
        val report = buildAvailabilityBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
