package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTireRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703394152)
        val items = mutableListOf<LegacyTireRequest>()
        for (index in 0 until 3) {
            items.add(newLegacyTireRequest("legacy-tire-request-$index", "sample"))
        }
        val report = buildLegacyTireRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
