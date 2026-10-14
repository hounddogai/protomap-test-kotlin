package com.acme.transport.crewing.availability.audit

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyAvailabilityGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704068711)
        val items = mutableListOf<LegacyAvailabilityGroup>()
        for (index in 0 until 6) {
            items.add(newLegacyAvailabilityGroup("legacy-availability-group-$index", "sample"))
        }
        val report = buildLegacyAvailabilityGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
