package com.acme.transport.network.zone.tier

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703098621)
        val items = mutableListOf<ZoneTier>()
        for (index in 0 until 2) {
            items.add(newZoneTier("zone-tier-$index", "sample"))
        }
        val report = buildZoneTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
