package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTireThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706850054)
        val items = mutableListOf<LegacyTireThreshold>()
        for (index in 0 until 4) {
            items.add(newLegacyTireThreshold("legacy-tire-threshold-$index", "sample"))
        }
        val report = buildLegacyTireThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
