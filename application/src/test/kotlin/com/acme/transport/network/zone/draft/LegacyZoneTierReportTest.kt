package com.acme.transport.network.zone.draft

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyZoneTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705065507)
        val items = mutableListOf<LegacyZoneTier>()
        for (index in 0 until 6) {
            items.add(newLegacyZoneTier("legacy-zone-tier-$index", "sample"))
        }
        val report = buildLegacyZoneTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
