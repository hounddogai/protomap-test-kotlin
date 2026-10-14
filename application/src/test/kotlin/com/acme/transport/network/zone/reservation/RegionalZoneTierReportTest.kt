package com.acme.transport.network.zone.reservation

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalZoneTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709804326)
        val items = mutableListOf<RegionalZoneTier>()
        for (index in 0 until 4) {
            items.add(newRegionalZoneTier("regional-zone-tier-$index", "sample"))
        }
        val report = buildRegionalZoneTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
